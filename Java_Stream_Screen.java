import com.xuggle.xuggler.Configuration;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class XugglerRtmpReferenceImpl {

   private static String url = "rtmp://your.test.server/screen/";
   private static String fileName = "test/teststream";
   private static int framesToEncode = 60;
   private static int x = 0;
   private static int y = 0;
   private static int height = 480;
   private static int width = 640;

   public static void main(String[] args) {
       IContainer container = IContainer.make();
       IContainerFormat containerFormat_live = IContainerFormat.make();
       containerFormat_live.setOutputFormat("flv", url + fileName, null);
       container.setInputBufferLength(0);
       int retVal = container.open(url + fileName, IContainer.Type.WRITE, containerFormat_live);
       if (retVal < 0) {
           System.err.println("Could not open output container for live stream");
           System.exit(1);
       }
       IStream stream = container.addNewStream(0);
       IStreamCoder coder = stream.getStreamCoder();
       ICodec codec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_H264);
       coder.setNumPicturesInGroupOfPictures(5);
       coder.setCodec(codec);
       coder.setBitRate(200000);
       coder.setPixelType(IPixelFormat.Type.YUV420P);
       coder.setHeight(height);
       coder.setWidth(width);
       System.out.println("[ENCODER] video size is " + width + "x" + height);
       coder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
       coder.setGlobalQuality(0);
       IRational frameRate = IRational.make(5, 1);
       coder.setFrameRate(frameRate);
       coder.setTimeBase(IRational.make(frameRate.getDenominator(), frameRate.getNumerator()));
       Properties props = new Properties();
       InputStream is = XugglerRtmpReferenceImpl.class.getResourceAsStream("/libx264-normal.ffpreset");
       try {
           props.load(is);
       } catch (IOException e) {
           System.err.println("You need the libx264-normal.ffpreset file from the Xuggle distribution in your classpath.");
           System.exit(1);
       }
       Configuration.configure(props, coder);
       coder.open();
       container.writeHeader();
       long firstTimeStamp = System.currentTimeMillis();
       long lastTimeStamp = -1;
       int i = 0;
       try {
           Robot robot = new Robot();
           while (i < framesToEncode) {
               //long iterationStartTime = System.currentTimeMillis();
               long now = System.currentTimeMillis();
               //grab the screenshot
               BufferedImage image = robot.createScreenCapture(new Rectangle(x, y, width, height));
               //convert it for Xuggler
               BufferedImage currentScreenshot = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
               currentScreenshot.getGraphics().drawImage(image, 0, 0, null);
               //start the encoding process
               IPacket packet = IPacket.make();
               IConverter converter = ConverterFactory.createConverter(currentScreenshot, IPixelFormat.Type.YUV420P);
               long timeStamp = (now - firstTimeStamp) * 1000; 
               IVideoPicture outFrame = converter.toPicture(currentScreenshot, timeStamp);
               if (i == 0) {
                   //make first frame keyframe
                   outFrame.setKeyFrame(true);
               }
               outFrame.setQuality(0);
               coder.encodeVideo(packet, outFrame, 0);
               outFrame.delete();
               if (packet.isComplete()) {
                   container.writePacket(packet);
                   System.out.println("[ENCODER] writing packet of size " + packet.getSize() + " for elapsed time " + ((timeStamp - lastTimeStamp) / 1000));
                   lastTimeStamp = timeStamp;
               }
               System.out.println("[ENCODER] encoded image " + i + " in " + (System.currentTimeMillis() - now));
               i++;
               try {
                   Thread.sleep(Math.max((long) (1000 / frameRate.getDouble()) - (System.currentTimeMillis() - now), 0));
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
       } catch (AWTException e) {
           e.printStackTrace();
       }
       container.writeTrailer();
    }
}

/* With Audio
        // container setup
        IContainer container = IContainer.make();
        IContainerFormat containerFormat = IContainerFormat.make();
        containerFormat.setOutputFormat("flv", streamUrl, null);
        container.setInputBufferLength(0);
        int retVal = container.open(streamUrl, IContainer.Type.WRITE, containerFormat);
        if (retVal < 0) {
            System.err.println("Could not open output container for live stream");
        }

        // video stream/encoder setup
        IStream videoStream = container.addNewStream(0);
        IStreamCoder videoEncoder = videoStream.getStreamCoder();
        videoEncoder.setNumPicturesInGroupOfPictures(5);
        videoEncoder.setCodec(ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_H264));
        videoEncoder.setBitRate(200000);
        videoEncoder.setPixelType(IPixelFormat.Type.YUV420P);
        videoEncoder.setHeight(IMAGE_HEIGHT_PX_OUTPUT);
        videoEncoder.setWidth(IMAGE_WIDTH_PX_OUTPUT);
        videoEncoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
        videoEncoder.setGlobalQuality(0);
        IRational frameRate = IRational.make(30, 1);
        videoEncoder.setFrameRate(IRational.make(30, 1));
        videoEncoder.setTimeBase(IRational.make(frameRate.getDenominator(), frameRate.getNumerator()));

        // audio stream/encoder setup
        IStream audioStream = container.addNewStream(1);
        IStreamCoder audioEncoder = audioStream.getStreamCoder();
        ICodec audioCodec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_AAC);
        audioEncoder.setCodec(audioCodec);
        audioEncoder.setBitRate(128 * 1024);
        audioEncoder.setChannels(1);
        audioEncoder.setSampleRate(44100);

        // configure encoders
        Properties props = new Properties();
        InputStream is = PollService.class.getResourceAsStream("/libx264-normal.ffpreset");
        try {
            props.load(is);
        } catch (IOException e) {
            System.err.println("You need the libx264-normal.ffpreset file from the Xuggle distribution in your classpath.");
        }
        Configuration.configure(props, videoEncoder);
        Configuration.configure(props, audioEncoder);

        // audio decoding
        String inputAudioFilePath = "silence.mp3";
        IContainer inputAudioContainer = IContainer.make();
        inputAudioContainer.open(inputAudioFilePath, IContainer.Type.READ, null);
        IStreamCoder audioDecoder = inputAudioContainer.getStream(0).getStreamCoder();
        audioDecoder.setCodec(ICodec.ID.CODEC_ID_MP3);

        // open
        audioDecoder.open();
        videoEncoder.open();
        audioEncoder.open();
        container.writeHeader();

        // prep for loop
        long firstTimeStamp = System.currentTimeMillis();
        long lastTimeStamp = System.currentTimeMillis();
        long lastKeyFrameTimestamp = 0;
        int i = 0;
        
        while (streaming) {
            
            long now = System.currentTimeMillis();
            
            //convert bufferedImage
            BufferedImage convertedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            convertedImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
            
            //start the encoding process
            IPacket videoPacket = IPacket.make();

            IConverter converter = ConverterFactory.createConverter(convertedImage, IPixelFormat.Type.YUV420P);
            long timeStamp = (now - firstTimeStamp) * 1000;
            IVideoPicture outFrame = converter.toPicture(convertedImage, timeStamp);

            // make sure there is a keyframe at least every 2 seconds
            if (System.currentTimeMillis() - lastKeyFrameTimestamp > 1500) {
                outFrame.setKeyFrame(true);
                lastKeyFrameTimestamp = System.currentTimeMillis();
            }
            outFrame.setQuality(0);
            videoEncoder.encodeVideo(videoPacket, outFrame, 0);
            System.out.println("[VIDEO-ENCODER] encoded image " + i + " in " + (System.currentTimeMillis() - now));
            outFrame.delete();
            
            // attempt to write video packet
            if (videoPacket.isComplete()) {
                container.writePacket(videoPacket);
                System.out.println("[VIDEO-ENCODER] writing packet of size " + videoPacket.getSize() + " for elapsed time " + ((timeStamp - lastTimeStamp) / 1000));
                lastTimeStamp = timeStamp;
            } else {
                System.out.println("[VIDEO-ENCODER] not writing packet, not complete");
            }

            // decode audio from mp3 and encode it into the out packet
            IPacket inAudioPacket = IPacket.make();
            IPacket outAudioPacket = IPacket.make();
            IAudioSamples samples = IAudioSamples.make(512, audioDecoder.getChannels());
            inputAudioContainer.readNextPacket(inAudioPacket);
            audioDecoder.decodeAudio(samples, inAudioPacket, 0);
            audioEncoder.encodeAudio(outAudioPacket, samples, 0);

            // attempt to write audio packet
            if (outAudioPacket.isComplete()) {
                container.writePacket(outAudioPacket);
                System.out.println("[AUDIO-ENCODER] writing packet of size " + outAudioPacket.getSize());
            } else {
                System.out.println("[AUDIO-ENCODER] not writing packet, not complete");
            }

            // prep for iteration
            i++;
            try {
                Thread.sleep(Math.max((long) (1000 / frameRate.getDouble()) - (System.currentTimeMillis() - now), 0));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        container.writeTrailer();
*/
