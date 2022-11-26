import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ParallelPerlinNoise {
    // Java version of Perlin Noise Generator with code from Perlin Noise Wikipedia page as reference
    // https://en.wikipedia.org/wiki/Perlin_noise#:~:text=Perlin%20noise%20is%20a%20procedural,details%20are%20the%20same%20size.
    // https://github.com/rtouti/rtouti.github.io/blob/gh-pages/examples/perlin-noise.html
    // Original Perlin Noise Algorithm: https://cs.nyu.edu/~perlin/noise/
    public static void main(String[] args) throws InterruptedException, IOException {
        FileWriter f = new FileWriter("noise.csv");
        int width = 512, height = 512; // power of 2 starting from 4
        double[][] coords = new double[width][height];
        ArrayList<Thread> threads = new ArrayList<Thread>();
        // generate 2D array representing grid with random vectors at each coordinate (corner)
        System.out.println("Dispatching work to threads...");
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Thread thr = new Thread(new PerlinThread(coords, x*0.01, y*0.01, x, y));
                threads.add(thr);
                thr.start();
            }
        }
        // wait for all threads to die before proceeding
        System.out.println("Waiting for threads to finish...");
        for(Thread t : threads) {
            t.join();
        }
        // write noise values to output file
        System.out.println("Work Completed! Writing to file...");
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Double val = (coords[y][x] + 1.0) / 2.0; // clamp value to range between 0.0 and 1.0
                String line = val.toString() + "\n";
                f.write(line);
            }
        }
        f.close();
        System.out.println("File finished writing!");
    }
}
class PerlinThread implements Runnable {
    private double[][] coords;
    private double x, y;
    private int ax, ay;

    /**
     * Initialize thread with coordinate array and "noised" and true x and y coordinates
     * @param coords the coordinate array to store completed dot product in
     * @param x "noised" x coordinate
     * @param y "noised" y coordinate
     * @param ax true x coordinate
     * @param ay true y coordinate
     */
    public PerlinThread(double[][] coords, double x, double y, int ax, int ay) {
        this.coords = coords;
        this.x = x;
        this.y = y;
        this.ax = ax;
        this. ay = ay;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        Vector2D topRight = new Vector2D(xf - 1.0, yf - 1.0);
        Vector2D topLeft = new Vector2D(xf, yf - 1.0);
        Vector2D bottomRight = new Vector2D(xf - 1.0, yf);
        Vector2D bottomLeft = new Vector2D(xf, yf);

        double dotTopRight = topRight.dot(randVect());
        double dotTopLeft = topLeft.dot(randVect());
        double dotBottomRight = bottomRight.dot(randVect());
        double dotBottomLeft = bottomLeft.dot(randVect());

        double u = fade(xf);
        double v = fade(yf);

        coords[ax][ay] = interpolate(u, interpolate(v, dotBottomLeft, dotTopLeft), interpolate(v, dotBottomRight, dotTopRight));
    }
    
    /**
     * Linearly interpolates two numbers
     * https://rtouti.github.io/graphics/perlin-noise-algorithm
     * @param w weight of interpolation
     * @param a1 first value to interpolate
     * @param a2 second value to interpolate
     * @return interpolated value as a double
     */
    private double interpolate(double w, double a1, double a2) {
        return a1 + w*(a2-a1);
    }

    /**
     * Generate a random 2D vector
     * @return Vector2D object with random x and y values between 0.0 and 1.0
     */
    private Vector2D randVect() {
        // random vectors with length between 0 and 1 units, inclusive
        // https://stackoverflow.com/questions/8147441/java-get-the-coordinate-with-random-angle
        double dx = Math.toRadians(Math.random() * 360);
        double dy = Math.toRadians(Math.random() * 360);
        return new Vector2D( Math.cos(dx), Math.sin(dy) );
    }

    /**
     * fade function to make interpolation easier to understand
     * 6t^5 * 15t^4 + 10t^3
     * https://rtouti.github.io/graphics/perlin-noise-algorithm
     * @param val value to fade
     * @return the smoothed value
     */
    private double fade(double val) {
        return ((6*val - 15) * val + 10) * val * val * val;
    }

}
class Vector2D {
    public double x;
    public double y;
    /**
     * Initialize 2D vector with specified x and y coordinates
     * @param x the x coordinate of the vector
     * @param y the y coordinate of the vector
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculates the dot product between this vector and another
     * @param other other Vector2D object to perform operation on
     * @return dot product between the two vectors as a double
     */
    public double dot(Vector2D other) {
        return (x * other.x + y * other.y);
    }
}