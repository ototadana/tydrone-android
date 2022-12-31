package com.xpfriend.tydrone.sensor;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2RGB;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

import com.xpfriend.tydrone.core.Info;
import com.xpfriend.tydrone.core.Startable;
import com.xpfriend.tydrone.core.VideoFrame;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;

public class DepthSensor extends Startable {
    @Override
    public void start(Info info) throws Exception {
        super.start(info);

        Midas midas = new Midas(createInterpreter());

        while (info.isActive()) {
            if (!info.isEnabled(DepthSensor.class.getSimpleName())) {
                sleep(100);
                continue;
            }

            VideoFrame<Frame> vf = info.getFrame();
            Frame frame = vf != null ? vf.get() : null;
            if (frame != null) {
                //long start = System.currentTimeMillis();
                int area = midas.getFocusArea(frame);
                //logi("nearest:" + area + ", " + (System.currentTimeMillis() - start));
                info.setState("nearest", String.valueOf(area));
            }
        }
    }

    protected Interpreter createInterpreter() throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(2);

        File file = new File("/storage/emulated/0/Download/midasi.tflite");
        if (!file.isFile()) {
            throw new NoSuchFileException(file.getAbsolutePath());
        }

        try (FileInputStream is = new FileInputStream(file)) {
            FileChannel channel = is.getChannel();
            MappedByteBuffer model = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            return new Interpreter(model, options);
        }
    }

    static class Midas {
        private final Interpreter interpreter;
        private final OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
        private final Tensor input;
        private final Tensor output;
        private final ByteBuffer outputBuffer;

        public Midas(Interpreter interpreter) {
            interpreter.allocateTensors();
            this.interpreter = interpreter;
            this.input = interpreter.getInputTensor(0);
            this.output = interpreter.getOutputTensor(0);
            this.outputBuffer = ByteBuffer.allocate(256 * 256 * 3);
        }

        public int getFocusArea(Frame frame) {
            Object input = prepareInput(frame);
            interpreter.run(input, outputBuffer.clear());
            outputBuffer.flip();
            return getFocusArea(outputBuffer);
        }

        private int getFocusArea(ByteBuffer output) {
            int width = width(this.output);
            int height = height(this.output);

            int[] values = new int[8];
            int step = width / values.length;
            int index = 0;
            for (int x = 0; x < width; x++) {
                int nextStep = (index + 1) * step;
                if (x >= nextStep) {
                    index++;
                }
                if (index >= values.length) {
                    break;
                }
                for (int y = 0; y < height; y++) {
                    values[index] += output.get(y * width + x);
                }
            }

            int max1 = Integer.MIN_VALUE, max2 = Integer.MIN_VALUE, max3 = Integer.MIN_VALUE;
            int idx1 = -1, idx2 = -1, idx3 = -1;
            for (int i = 0; i < values.length; i++) {
                if (values[i] > max1) {
                    max3 = max2;
                    max2 = max1;
                    max1 = values[i];
                    idx3 = idx2;
                    idx2 = idx1;
                    idx1 = i;
                } else if (values[i] > max2) {
                    max3 = max2;
                    max2 = values[i];
                    idx3 = idx2;
                    idx2 = i;
                } else if (values[i] > max3) {
                    max3 = values[i];
                    idx3 = i;
                }
            }
            //Log.i("DepthSensor", "DepthSensor:" + max1);
            int left = idx1 - 1;
            int right = idx1 + 1;
            if (idx1 == 0) {
                if (idx2 != right) {
                    return 0;
                }
            } else if (idx1 == 7) {
                if (idx2 != left) {
                    return 0;
                }
            } else {
                if (left != idx2 && left != idx3) {
                    return 0;
                }
                if (right != idx2 && right != idx3) {
                    return 0;
                }
            }

            return idx1 > 3 ? idx1 - 3 : idx1 - 4;
        }

        private ByteBuffer prepareInput(Frame frame) {
            Mat cvImg = matConverter.convertToMat(frame);
            Mat orgImg = new Mat(cvImg.rows(), cvImg.cols(), cvImg.type());
            cvtColor(cvImg, orgImg, COLOR_BGR2RGB);
            Size size = new Size(width(input), height(input));
            Mat image = new Mat();
            resize(orgImg, image, size, 0, 0, INTER_CUBIC);
            return image.createBuffer();
        }

        private int height(Tensor tensor) {
            return tensor.shape()[1];
        }

        private int width(Tensor tensor) {
            return tensor.shape()[2];
        }
    }
}
