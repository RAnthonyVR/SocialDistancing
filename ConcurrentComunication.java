import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;

public class ConcurrentComunication  {
    
    private String currentImage;

    public synchronized String receiveImage (String lastProcessedImage) {
        while (lastProcessedImage == currentImage) {
            try {
                wait();
            } catch (InterruptedException ie);
        }
        System.out.println("Image name received by image processor: " + currentImage);
        return this.currentImage;
    }

    public synchronized void sendImage (String currentImage) {
        System.out.println("Image name sent by camera: " + currentImage);
        this.currentImage = currentImage;
        notifyAll();
    }

}