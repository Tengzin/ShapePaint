import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.io.*;
import java.awt.image.*;
import org.apache.commons.io.FilenameUtils;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class Drawing extends JPanel implements KeyListener {
    Shape shape;
    public static JLabel tool = new JLabel("select");
    public static Color color = Color.BLACK;
    public static int thickness = 2;
    Point M = new Point();
    ArrayList<Shape> shapeList = new ArrayList<Shape>();
    Shape selectedShape;
    Drawing() {
        addKeyListener(this);
        this.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e) {
                if (tool.getText() == "oval" || tool.getText() == "square" || tool.getText() == "line") {
                    Shape currShape = new Shape();
                    shape = currShape;
                    shapeList.add(shape);
                    // change shape type
                    int type = 0;
                    if (tool.getText() == "oval") type = 1;
                    else if (tool.getText() == "square") type = 2;
                    else type = 3;
                    shape.setWhatShape(type);
                    shape.setColour(color);
                    shape.setThickness(thickness);
                    shape.setIsDrawn(true);
                    shape.setScale(1.0f);
                    repaint();
                } else if (tool.getText() == "eraser") {
                    if (shape != null){
                        for (Shape shape: shapeList) {
                            if (shape.hitTest(M.x, M.y) == true) {
                                shapeList.remove(shape);
                                repaint();
                                break;
                            }
                        }
                    }
                } else if (tool.getText() == "fill") {
                    if (shape != null){
                        for (Shape shape: shapeList) {
                            if (shape.hitTest(M.x, M.y) == true) {
                                shape.setColour(color);
                                repaint();
                                break;
                            }
                        }
                    }
                } else if (tool.getText() == "select") {
                    if (shape != null){
                        for (Shape shape: shapeList) {
                            if (shape.hitTest(M.x, M.y) == true) {
                                if (selectedShape != null && selectedShape != shape) {
                                    selectedShape.setIsSelected(false);
                                }
                                selectedShape = shape;
                                shape.setIsSelected(true);
                                shape.setColour(color);
                                shape.setThickness(thickness);
                                repaint();
                                break;
                            }
                        }
                    }
                }
            }
        });
        this.addMouseMotionListener(new MouseAdapter(){
            public void mouseDragged(MouseEvent e) {
                if (tool.getText() == "oval" || tool.getText() == "square" || tool.getText() == "line") {
                    shape.addPoint(e.getX(), e.getY());
                    repaint();
                }
                else if (tool.getText() == "select") {
                    if (selectedShape != null){
                        selectedShape.translate(e.getX(), e.getY());
                        repaint();
                    }
                }
            }
        });
        this.addMouseMotionListener(new MouseAdapter(){
            public void mouseMoved(MouseEvent e){
                M.x = e.getX();
                M.y = e.getY();
                repaint();
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (selectedShape != null) {
                selectedShape.setIsSelected(false);
                repaint();
                selectedShape = null;
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {}

    public void setColour(Color colour) {
        color = colour;
    }
    public void setSelectedColour(Color colour) {
        if (selectedShape != null) {
            selectedShape.setColour(colour);
            repaint();
        }
    }
    public void setSelectedThickness(int thick) {
        if (selectedShape != null) {
            selectedShape.setThickness(thick);
            repaint();
        }
    }
    public void setThickness(int thick) { thickness = thick;}
    public void resetCanvas() {
        shapeList.clear();
        repaint();
    }
    public void saveDrawing(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser(".");
        File file = new File("saved.txt");
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
            } else {
                file = new File(file.toString() + ".txt");
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".txt");
            }
            System.out.println("Save as file: " + file.getAbsolutePath());
        }
        try {
            PrintWriter writer = new PrintWriter(file);
            for (Shape shape: shapeList) {
                // Shape type
                int type = shape.getShape();
                writer.println(type);
                // Color
                String hex = Integer.toHexString(shape.getColour().getRGB() & 0xffffff);
                while (hex.length() < 6) {
                    hex = "0" + hex;
                }
                hex = "#" + hex;
                writer.println(hex);
                // Line width
                int width = shape.getThickness();
                writer.println(width);
                writer.println(shape.getX1());
                writer.println(shape.getX2());
                writer.println(shape.getY1());
                writer.println(shape.getY2());

                int numPoints = shape.getNumPoints();
                writer.println(numPoints);
                int x[] = shape.getXpoints();
                int y[] = shape.getYpoints();
                for (int i = 0; i < numPoints; i++) {
                    writer.println(x[i]);
                    writer.println(y[i]);
                }
            }
            writer.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }
    public void loadCanvas(JFrame frame) {
        File file = new File("dummy");
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Specify a file to open (must be .txt)");
        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                Shape createShape = new Shape();
                createShape.setWhatShape(Integer.parseInt(line));
                line = br.readLine();
                createShape.setColour(Color.decode(line));
                line = br.readLine();
                createShape.setThickness(Integer.parseInt(line));
                line = br.readLine();
                createShape.setX1(Integer.parseInt(line));
                line = br.readLine();
                createShape.setX2(Integer.parseInt(line));
                line = br.readLine();
                createShape.setY1(Integer.parseInt(line));
                line = br.readLine();
                createShape.setY2(Integer.parseInt(line));
                line = br.readLine();
                int size = Integer.parseInt(line);
                createShape.setPointArrays(size);
                for (int i = 0; i < size; i++) {
                    line = br.readLine();
                    int x = Integer.parseInt(line);
                    line = br.readLine();
                    int y = Integer.parseInt(line);
                    createShape.setPoints(x, y, i);
                }
                shape = createShape;
                shapeList.add(shape);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        repaint();
    }
    public void copy() {
        BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        this.printAll(g2d);
        g2d.dispose();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        ImageTransferable selection = new ImageTransferable(img);
        clipboard.setContents(selection, null);
    }
    public static void main(String[] args) {
        // create a window
        JFrame frame = new JFrame("JSketch");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setFocusable(false);

        // main panel and canvas
        Drawing canvas = new Drawing();
        canvas.setFocusable(true);
        canvas.setBorder(BorderFactory.createLineBorder(Color.black));
        BorderLayoutWrap mainPanel = new BorderLayoutWrap(canvas, tool, frame);
        mainPanel.add(canvas, BorderLayout.CENTER);
        mainPanel.setFocusable(false);
        // add panel to the window
        frame.add(mainPanel);
        // set window behaviour and display it
        frame.setResizable(false);
        frame.setSize(900, 650);
        frame.setVisible(true);
        canvas.requestFocusInWindow();
    }

    // Template for paint was obtained from ShapeDemo.java from graphics sample code
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g; // cast to get 2D drawing methods
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (shape != null)
            for (Shape shape: shapeList) {
                shape.draw(g2);
                if (tool.getText() == "eraser" || tool.getText() == "select") shape.hitTest(M.x, M.y);
            }
    }
}

// From https://alvinalexander.com/java/java-copy-image-to-clipboard-example 
class ImageTransferable implements Transferable
{
    public ImageTransferable(Image image)
    {
        theImage = image;
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[] { DataFlavor.imageFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor.equals(DataFlavor.imageFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor.equals(DataFlavor.imageFlavor))
        {
            return theImage;
        }
        else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }
    private Image theImage;
}

class BorderLayoutWrap extends JPanel {
    public BorderLayoutWrap(Drawing canvas, JLabel tool, JFrame frame) {
        // use BorderLayout
        this.setLayout(new BorderLayout());

        // Add the components
        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setMinimumSize(new Dimension(200, 30));
        JMenu menu = new JMenu("File");
        // template for shape class was obtained from WidgetDemo.java from widgets sample code
        for (String s: new String[] {"New", "Copy", "Save", "Load" })
        {
            JMenuItem mi = new JMenuItem(s);
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JMenuItem mi = (JMenuItem)e.getSource();
                    String command = mi.getText();
                    if (command == "New" || command == "Load") {
                        canvas.resetCanvas();
                    } else if (command == "Save") {
                        canvas.saveDrawing(frame);
                    } else if (command == "Copy") {
                        canvas.copy();
                    }
                    if (command == "Load") {
                        canvas.loadCanvas(frame);
                    }
                }
            });
            // add this menu item to the menu
            menu.add(mi);
        }
        JMenuBar menubar = new JMenuBar();
        menubar.add(menu);
        topPanel.add(menubar);

        this.add(topPanel, BorderLayout.NORTH);

        // Left side with tools
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        leftPanel.setMinimumSize(new Dimension(160, 650));
        leftPanel.setMaximumSize(new Dimension(160, 650));
        leftPanel.setFocusable(false);

        // Tool picker
        JPanel tools = new JPanel();
        tools.setFocusable(false);
        tools.setLayout(new GridLayout(3,2));
        tools.setPreferredSize(new Dimension(160, 210));
        tools.setMinimumSize(new Dimension(150, 260));
        tools.setMaximumSize(new Dimension(200, 280));
        JButton cursor = new JButton();
        JButton eraser = new JButton();
        JButton lineTool = new JButton();
        JButton circleTool = new JButton();
        JButton squareTool = new JButton();
        JButton fillTool = new JButton();

        cursor.setFocusable(false);
        eraser.setFocusable(false);
        lineTool.setFocusable(false);
        circleTool.setFocusable(false);
        squareTool.setFocusable(false);
        fillTool.setFocusable(false);

        cursor.setBorder(BorderFactory.createLineBorder(Color.cyan));
        eraser.setBorder(BorderFactory.createLineBorder(Color.black));
        lineTool.setBorder(BorderFactory.createLineBorder(Color.black));
        circleTool.setBorder(BorderFactory.createLineBorder(Color.black));
        squareTool.setBorder(BorderFactory.createLineBorder(Color.black));
        fillTool.setBorder(BorderFactory.createLineBorder(Color.black));
        cursor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tool.setText("select");
                cursor.setBorder(BorderFactory.createLineBorder(Color.cyan));
                eraser.setBorder(BorderFactory.createLineBorder(Color.black));
                lineTool.setBorder(BorderFactory.createLineBorder(Color.black));
                circleTool.setBorder(BorderFactory.createLineBorder(Color.black));
                squareTool.setBorder(BorderFactory.createLineBorder(Color.black));
                fillTool.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        });
        eraser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tool.setText("eraser");
                cursor.setBorder(BorderFactory.createLineBorder(Color.black));
                eraser.setBorder(BorderFactory.createLineBorder(Color.cyan));
                lineTool.setBorder(BorderFactory.createLineBorder(Color.black));
                circleTool.setBorder(BorderFactory.createLineBorder(Color.black));
                squareTool.setBorder(BorderFactory.createLineBorder(Color.black));
                fillTool.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        });
        lineTool.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tool.setText("line");
                cursor.setBorder(BorderFactory.createLineBorder(Color.black));
                eraser.setBorder(BorderFactory.createLineBorder(Color.black));
                lineTool.setBorder(BorderFactory.createLineBorder(Color.cyan));
                circleTool.setBorder(BorderFactory.createLineBorder(Color.black));
                squareTool.setBorder(BorderFactory.createLineBorder(Color.black));
                fillTool.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        });
        circleTool.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tool.setText("oval");
                cursor.setBorder(BorderFactory.createLineBorder(Color.black));
                eraser.setBorder(BorderFactory.createLineBorder(Color.black));
                lineTool.setBorder(BorderFactory.createLineBorder(Color.black));
                circleTool.setBorder(BorderFactory.createLineBorder(Color.cyan));
                squareTool.setBorder(BorderFactory.createLineBorder(Color.black));
                fillTool.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        });
        squareTool.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tool.setText("square");
                cursor.setBorder(BorderFactory.createLineBorder(Color.black));
                eraser.setBorder(BorderFactory.createLineBorder(Color.black));
                lineTool.setBorder(BorderFactory.createLineBorder(Color.black));
                circleTool.setBorder(BorderFactory.createLineBorder(Color.black));
                squareTool.setBorder(BorderFactory.createLineBorder(Color.cyan));
                fillTool.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        });
        fillTool.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tool.setText("fill");
                cursor.setBorder(BorderFactory.createLineBorder(Color.black));
                eraser.setBorder(BorderFactory.createLineBorder(Color.black));
                lineTool.setBorder(BorderFactory.createLineBorder(Color.black));
                circleTool.setBorder(BorderFactory.createLineBorder(Color.black));
                squareTool.setBorder(BorderFactory.createLineBorder(Color.black));
                fillTool.setBorder(BorderFactory.createLineBorder(Color.cyan));
            }
        });
        try {
            Image cursorImg = ImageIO.read(getClass().getResource("newcursor.png"));
            cursor.setIcon(new ImageIcon(cursorImg));
            Image eraserImg = ImageIO.read(getClass().getResource("eraser2.png"));
            eraser.setIcon(new ImageIcon(eraserImg));
            Image lineImg = ImageIO.read(getClass().getResource("line.png"));
            lineTool.setIcon(new ImageIcon(lineImg));
            Image circleImg = ImageIO.read(getClass().getResource("ellipse.png"));
            circleTool.setIcon(new ImageIcon(circleImg));
            Image recImg = ImageIO.read(getClass().getResource("rect.png"));
            squareTool.setIcon(new ImageIcon(recImg));
            Image fillImg = ImageIO.read(getClass().getResource("bucket.png"));
            fillTool.setIcon(new ImageIcon(fillImg));
        } catch (Exception ex) {
            System.out.println(ex);
        }
        tools.add(cursor);
        tools.add(eraser);
        tools.add(lineTool);
        tools.add(circleTool);
        tools.add(squareTool);
        tools.add(fillTool);
        leftPanel.add(tools);

        // Colour picker
        JPanel colourPicker = new JPanel();
        JButton chooser = new JButton("Chooser");
        colourPicker.setLayout(new GridLayout(3,2));
        chooser.setFocusable(false);
        colourPicker.setFocusable(false);

        JButton blueButton = new JButton();
        blueButton.setOpaque(true);
        blueButton.setBackground(Color.BLUE);
        JButton redButton = new JButton();
        redButton.setOpaque(true);
        redButton.setBackground(Color.RED);
        JButton blackButton = new JButton();
        blackButton.setOpaque(true);
        blackButton.setBackground(Color.BLACK);
        JButton orangeButton = new JButton();
        orangeButton.setOpaque(true);
        orangeButton.setBackground(Color.ORANGE);
        JButton pinkButton = new JButton();
        pinkButton.setOpaque(true);
        pinkButton.setBackground(Color.PINK);
        JButton greenButton = new JButton();
        greenButton.setOpaque(true);
        greenButton.setBackground(Color.green);

        blueButton.setFocusable(false);
        redButton.setFocusable(false);
        blackButton.setFocusable(false);
        orangeButton.setFocusable(false);
        pinkButton.setFocusable(false);
        greenButton.setFocusable(false);

        blueButton.setBorder(BorderFactory.createLineBorder(Color.blue));
        redButton.setBorder(BorderFactory.createLineBorder(Color.red));
        blackButton.setBorder(BorderFactory.createLineBorder(Color.cyan));
        orangeButton.setBorder(BorderFactory.createLineBorder(Color.orange));
        pinkButton.setBorder(BorderFactory.createLineBorder(Color.pink));
        greenButton.setBorder(BorderFactory.createLineBorder(Color.green));

        blueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setColour(Color.BLUE);
                chooser.setBorder(BorderFactory.createLineBorder(Color.black,3));
                blueButton.setBorder(BorderFactory.createLineBorder(Color.cyan));
                canvas.setSelectedColour(Color.blue);
                redButton.setBorder(BorderFactory.createLineBorder(Color.red));
                blackButton.setBorder(BorderFactory.createLineBorder(Color.black));
                orangeButton.setBorder(BorderFactory.createLineBorder(Color.orange));
                pinkButton.setBorder(BorderFactory.createLineBorder(Color.pink));
                greenButton.setBorder(BorderFactory.createLineBorder(Color.green));
            }
        });
        redButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setColour(Color.RED);
                chooser.setBorder(BorderFactory.createLineBorder(Color.black,3));
                blueButton.setBorder(BorderFactory.createLineBorder(Color.blue));
                redButton.setBorder(BorderFactory.createLineBorder(Color.cyan));
                canvas.setSelectedColour(Color.red);
                blackButton.setBorder(BorderFactory.createLineBorder(Color.black));
                orangeButton.setBorder(BorderFactory.createLineBorder(Color.orange));
                pinkButton.setBorder(BorderFactory.createLineBorder(Color.pink));
                greenButton.setBorder(BorderFactory.createLineBorder(Color.green));
            }
        });
        blackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setColour(Color.BLACK);
                chooser.setBorder(BorderFactory.createLineBorder(Color.black, 3));
                blueButton.setBorder(BorderFactory.createLineBorder(Color.blue));
                redButton.setBorder(BorderFactory.createLineBorder(Color.red));
                blackButton.setBorder(BorderFactory.createLineBorder(Color.cyan));
                canvas.setSelectedColour(Color.black);
                orangeButton.setBorder(BorderFactory.createLineBorder(Color.orange));
                pinkButton.setBorder(BorderFactory.createLineBorder(Color.pink));
                greenButton.setBorder(BorderFactory.createLineBorder(Color.green));
            }
        });
        orangeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setColour(Color.ORANGE);
                chooser.setBorder(BorderFactory.createLineBorder(Color.black, 3));
                blueButton.setBorder(BorderFactory.createLineBorder(Color.blue));
                redButton.setBorder(BorderFactory.createLineBorder(Color.red));
                blackButton.setBorder(BorderFactory.createLineBorder(Color.black));
                orangeButton.setBorder(BorderFactory.createLineBorder(Color.cyan));
                canvas.setSelectedColour(Color.orange);
                pinkButton.setBorder(BorderFactory.createLineBorder(Color.pink));
                greenButton.setBorder(BorderFactory.createLineBorder(Color.green));
            }
        });
        pinkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setColour(Color.PINK);
                chooser.setBorder(BorderFactory.createLineBorder(Color.black, 3));
                blueButton.setBorder(BorderFactory.createLineBorder(Color.blue));
                redButton.setBorder(BorderFactory.createLineBorder(Color.red));
                blackButton.setBorder(BorderFactory.createLineBorder(Color.black));
                orangeButton.setBorder(BorderFactory.createLineBorder(Color.orange));
                pinkButton.setBorder(BorderFactory.createLineBorder(Color.cyan));
                canvas.setSelectedColour(Color.pink);
                greenButton.setBorder(BorderFactory.createLineBorder(Color.green));
            }
        });
        greenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setColour(Color.green);
                chooser.setBorder(BorderFactory.createLineBorder(Color.black, 3));
                blueButton.setBorder(BorderFactory.createLineBorder(Color.blue));
                redButton.setBorder(BorderFactory.createLineBorder(Color.red));
                blackButton.setBorder(BorderFactory.createLineBorder(Color.black));
                orangeButton.setBorder(BorderFactory.createLineBorder(Color.orange));
                pinkButton.setBorder(BorderFactory.createLineBorder(Color.pink));
                greenButton.setBorder(BorderFactory.createLineBorder(Color.cyan));
                canvas.setSelectedColour(Color.green);
            }
        });

        colourPicker.add(blueButton);
        colourPicker.add(redButton);
        colourPicker.add(blackButton);
        colourPicker.add(orangeButton);
        colourPicker.add(pinkButton);
        colourPicker.add(greenButton);
        leftPanel.add(colourPicker);

        // Colour chooser
        JPanel colourChoosePanel = new JPanel();
        colourChoosePanel.setMaximumSize(new Dimension(150, 35));
        colourChoosePanel.setFocusable(false);
        try {
            Image picker = ImageIO.read(getClass().getResource("color.png"));
            chooser.setIcon(new ImageIcon(picker));
        } catch (Exception ex) {
            System.out.println(ex);
        }
        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(null, "Custom Colour", Color.BLACK);
                canvas.setColour(newColor);
                canvas.setSelectedColour(newColor);
                chooser.setBorder(BorderFactory.createLineBorder(Color.cyan, 3));
                blueButton.setBorder(BorderFactory.createLineBorder(Color.blue));
                redButton.setBorder(BorderFactory.createLineBorder(Color.red));
                blackButton.setBorder(BorderFactory.createLineBorder(Color.black));
                orangeButton.setBorder(BorderFactory.createLineBorder(Color.orange));
                pinkButton.setBorder(BorderFactory.createLineBorder(Color.pink));
                greenButton.setBorder(BorderFactory.createLineBorder(Color.green));
            }
        });
        chooser.setBorder(BorderFactory.createLineBorder(Color.black, 3));
        colourChoosePanel.add(chooser);
        leftPanel.add(colourChoosePanel);


        // Line chooser
        JPanel lineChooser = new JPanel();
        lineChooser.setFocusable(false);
        lineChooser.setMaximumSize(new Dimension(150, 30));
        JPanel lineChooser2 = new JPanel();
        lineChooser2.setMaximumSize(new Dimension(150, 30));
        lineChooser2.setFocusable(false);
        JPanel lineChooser3 = new JPanel();
        lineChooser3.setMaximumSize(new Dimension(150, 30));
        lineChooser3.setFocusable(false);
        JButton lineOne = new JButton();
        JButton lineTwo = new JButton();
        JButton lineThree = new JButton();
        lineOne.setFocusable(false);
        lineTwo.setFocusable(false);
        lineThree.setFocusable(false);
        try {
            Image img = ImageIO.read(getClass().getResource("line1.bmp"));
            lineOne.setIcon(new ImageIcon(img));
            Image img2 = ImageIO.read(getClass().getResource("line2.bmp"));
            lineTwo.setIcon(new ImageIcon(img2));
            Image img3 = ImageIO.read(getClass().getResource("line3.bmp"));
            lineThree.setIcon(new ImageIcon(img3));
        } catch (Exception ex) {
            System.out.println(ex);
        }
        LineBorder border1 = new LineBorder(Color.cyan);
        LineBorder border2 = new LineBorder(Color.black);
        EmptyBorder borderEmp = new EmptyBorder(4,5,4,5);
        Border chosenBorder = BorderFactory.createCompoundBorder(border1, borderEmp);
        Border notChosenBorder = BorderFactory.createCompoundBorder(border2, borderEmp);
        lineOne.setBorder(chosenBorder);
        lineTwo.setBorder(notChosenBorder);
        lineThree.setBorder(notChosenBorder);

        lineOne.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setThickness(3);
                canvas.setSelectedThickness(3);
                lineOne.setBorder(chosenBorder);
                lineTwo.setBorder(notChosenBorder);
                lineThree.setBorder(notChosenBorder);
            }
        });
        lineTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setThickness(6);
                canvas.setSelectedThickness(6);
                lineOne.setBorder(notChosenBorder);
                lineTwo.setBorder(chosenBorder);
                lineThree.setBorder(notChosenBorder);
            }
        });
        lineThree.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.setThickness(8);
                canvas.setSelectedThickness(8);
                lineOne.setBorder(notChosenBorder);
                lineTwo.setBorder(notChosenBorder);
                lineThree.setBorder(chosenBorder);
            }
        });

        lineChooser.add(lineOne);
        lineChooser2.add(lineTwo);
        lineChooser3.add(lineThree);
        leftPanel.add(lineChooser);
        leftPanel.add(lineChooser2);
        leftPanel.add(lineChooser3);
        this.add(leftPanel, BorderLayout.WEST);

    }

}

