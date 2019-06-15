import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.*;
import javax.vecmath.*;
import java.lang.*;

// Template for shape class was obtained from Shape.java from graphics sample code
class Shape {
    // shape points
    ArrayList<Point2d> points;

    public void clearPoints() {
        points = new ArrayList<Point2d>();
        pointsChanged = true;
    }

    // add a point to end of shape
    public void addPoint(Point2d p) {
        if (points == null) clearPoints();
        points.add(p);
        pointsChanged = true;
    }

    // add a point to end of shape
    public void addPoint(double x, double y) {
        if (points == null) clearPoints();
        addPoint(new Point2d(x, y));
    }

    public int npoints() {
        return points.size();
    }

    // What shape
    int whatShape;
    public int getShape() { return whatShape; }
    public void setWhatShape(int what) { whatShape = what; }

    // drawing attributes
    Color colour = Color.BLACK;
    float strokeThickness = 6.0f;

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    // Thickness
    int thickness = 2;
    public int getThickness() {
        return thickness;
    }
    public void setThickness(int thickness) { this.thickness = thickness;}

    // shape's transform
    float scale = 1.0f;

    public float getScale(){
        return scale;
    }

    public void setScale(float scale){
        this.scale = scale;
    }

    // some optimization to cache points for drawing
    Boolean pointsChanged = false; // dirty bit
    int[] xpoints, ypoints;
    int [] oldx, oldy;
    Boolean translated = false;
    int npoints = 0;

    void cachePointsArray() {
        xpoints = new int[points.size()];
        ypoints = new int[points.size()];
        for (int i=0; i < points.size(); i++) {
            xpoints[i] = (int)points.get(i).x;
            ypoints[i] = (int)points.get(i).y;
        }
        npoints = points.size();
        pointsChanged = false;
    }

    public int getNumPoints () { return npoints; }
    public int[] getXpoints () { return xpoints; }
    public int[] getYpoints () { return ypoints; }
    public void setPointArrays(int size) {
        xpoints = new int[size];
        ypoints = new int[size];
        npoints = size;
    }
    public void setPoints(int x, int y, int index) {
        xpoints[index] = x;
        ypoints[index] = y;
    }

    int x1 = -1, x2 = 0, y1 = 0, y2 = 0;
    int getX1() { return x1; }
    void setX1(int val) { x1 = val; }
    int getX2() { return x2; }
    void setX2(int val) { x2 = val; }
    int getY1() { return y1; }
    void setY1(int val) { y1 = val; }
    int getY2() { return y2; }
    void setY2(int val) { y2 = val; }

    Boolean isDrawn = false;
    void setIsDrawn(Boolean bool) { isDrawn = bool; }
    Ellipse2D.Double circle = new Ellipse2D.Double(x1, y1, x2-x1, y2-y1);
    Rectangle2D.Double rect = new Rectangle2D.Double(0,0,0,0);
    Line2D.Double line = new Line2D.Double(0, 0, 0, 0);

    Boolean isSelected = false;
    void setIsSelected(Boolean select) { isSelected = select; }

    public void draw(Graphics2D g2) {

        // don't draw if points are empty (not shape)
        if (points == null && x1 == -1) return;

        // see if we need to update the cache
        if (pointsChanged) cachePointsArray();

        if (translated == false) {
            oldx = xpoints.clone();
            oldy = ypoints.clone();
            int minX = Math.min(oldx[0], oldx[npoints-1]);
            int maxX = Math.max(oldx[0], oldx[npoints-1]);
            int minY = Math.min(oldy[0], oldy[npoints-1]);
            int maxY = Math.max(oldy[0], oldy[npoints-1]);
            for (int i = 0; i < npoints; i++) {
                oldx[i] -= (minX + maxX)/2;
                oldy[i] -= (minY+ maxY)/2;
            }
        }

        // save the current g2 transform matrix
        AffineTransform M = g2.getTransform();

        // multiply in this shape's transform
        // (uniform scale)
        g2.scale(scale, scale);

        // call drawing functions
        x1 = Math.min(xpoints[0], xpoints[npoints - 1]);
        x2 = Math.max(xpoints[0], xpoints[npoints - 1]);
        y1 = Math.min(ypoints[0], ypoints[npoints - 1]);
        y2 = Math.max(ypoints[0], ypoints[npoints - 1]);
        if (whatShape == 3) {
            x1 = xpoints[0];
            x2 = xpoints[npoints - 1];
            y1 = ypoints[0];
            y2 = ypoints[npoints- 1];
        }

        if (whatShape == 1) {
            if (isSelected == true) {
                g2.setColor(Color.cyan);
                g2.fillOval(x1-2, y1-2, x2-x1+4, y2-y1+4);
            }
            g2.setColor(Color.BLACK);
            g2.fillOval(x1, y1, x2-x1, y2-y1);
            g2.setColor(colour);
            g2.fillOval(x1+thickness, y1+thickness, x2-x1-(2*thickness), y2-y1-2*thickness);
            circle = new Ellipse2D.Double(x1, y1, x2-x1, y2-y1);
        } else if (whatShape == 2) {
            if (isSelected == true) {
                g2.setColor(Color.cyan);
                g2.fillRect(x1-2, y1-2, x2-x1+4, y2-y1+4);
            }
            g2.setColor(Color.BLACK);
            g2.fillRect(x1, y1, x2-x1, y2-y1);
            g2.setColor(colour);
            g2.fillRect(x1+thickness, y1+thickness, x2-x1-(2*thickness), y2-y1-2*thickness);
            rect = new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
        } else if (whatShape == 3) {
            g2.setStroke(new BasicStroke(thickness));
            if (isSelected == true) {
                g2.setColor(Color.cyan);
                g2.drawLine(x1-1, y1-3, x2+1, y2-3);
                g2.drawLine(x1-1, y1+3, x2+1, y2+3);
            }
            g2.setColor(colour);
            g2.drawLine(x1, y1, x2, y2);
            line = new Line2D.Double(x1, y1, x2, y2);
        }
        // reset the transform to what it was before we drew the shape
        g2.setTransform(M);
    }

    // let shape handle its own hit testing
    // (x,y) is the point to test against
    // (x,y) needs to be in same coordinate frame as shape, you could add
    // a panel-to-shape transform as an extra parameter to this function
    // (note this isn't good separation of shape Controller from shape Model)
    public boolean hitTest(double x, double y)
    {
//        if (points != null) {
            if (whatShape == 1) {
                if (circle.contains(x, y)) {
                    return true;
                }
            }
            else if (whatShape == 2) {
                if (rect.contains(x, y)) {
                    return true;
                }
            }
            else {
                double dist = line.ptSegDist(x, y);
                if (dist <= 5) return true;
            }

        return false;
    }
    public void translate(double tx, double ty) {
        translated = true;
        for (int i = 0; i < npoints; i++) {
            xpoints[i] = oldx[i] + (int) tx;
            ypoints[i] = oldy[i] + (int) ty;
        }
    }
}
