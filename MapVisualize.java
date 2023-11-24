import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapVisualize extends JFrame {
    private Map<String, Point> cityCoordinates;
    private Map<String, Map<String, Double>> distances;
    private String sourceCity = "";
    private String destinationCity = "";
    private float min_x = -108;
    private float max_x = -85;
    private float min_y = 25;
    private float max_y = 51;

    private int WIDTH = 1300;
    private int HEIGHT = 1000;

    private float convertCoords(float val, String type){
        if (type.equals("lngt")) return (val - min_x)/(max_x-min_x) * WIDTH;
        else if (type.equals("lat")) return HEIGHT - (val - min_y)/(max_y-min_y) * HEIGHT;
        return -1;
    }

    public MapVisualize(HashMap<String, City> citiesDS) {
        setTitle("City Map");
        setSize(WIDTH , HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Add a custom JPanel for drawing the map
        MapPanel mapPanel = new MapPanel(citiesDS);
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // mapPanel.add(scrollPane);
        add(scrollPane);

        // Add mouse motion listener for tooltips
        mapPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (String cityNameKey : citiesDS.keySet()) {
                    City city = citiesDS.get(cityNameKey);
                    float lat = city.lat;
                    float lngt = city.lngt;
                    if (new Ellipse2D.Double(convertCoords(lngt, "lngt") - 10, convertCoords(lat, "lat") - 10, 20, 20).contains(e.getPoint())) {
                        mapPanel.setToolTipText(city.name);
                        return;
                    }
                    Point point1 = new Point((int)convertCoords(lngt, "lngt"), (int)convertCoords(lat, "lat"));
                    for (CityConnectionStruct ccs : city.connections){
                            String connCityKey = StringStandardize.standardizeString(ccs.state) + "__" + StringStandardize.standardizeString(ccs.name);
                            City connCity = citiesDS.get(connCityKey);
                            if (connCity != null){
                                float conn_x = convertCoords(connCity.lngt, "lngt") ;
                                float conn_y  = convertCoords(connCity.lat, "lat");

                                Point point2 = new Point((int)conn_x, (int)conn_y) ;
                                if (isPointOnLine(e.getPoint(), point1, point2)) {
                                    double distance = ccs.distance;
                                    mapPanel.setToolTipText(String.format("Distance: %.2f", distance));

                                    return;
                                }

                            }
                        }
                }

                mapPanel.setToolTipText(null);
            }
        });

        // Add a panel for user input
        JPanel inputPanel = new JPanel();
        JTextField sourceField = new JTextField(10);
        JTextField destinationField = new JTextField(10);
        JButton enterButton = new JButton("Enter");
        // enterButton.addActionListener(new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         sourceCity = sourceField.getText();
        //         destinationCity = destinationField.getText();
        //         mapPanel.repaint(); // Repaint to update the path
        //     }
        // });
        inputPanel.add(new JLabel("Source City:"));
        inputPanel.add(sourceField);
        inputPanel.add(new JLabel("Destination City:"));
        inputPanel.add(destinationField);
        inputPanel.add(enterButton);

        // Add the input panel to the frame
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    class MapPanel extends JPanel {
        HashMap<String, City> citiesDS;
        MapPanel(HashMap<String, City> citiesDS) {
            this.citiesDS = citiesDS;
            setToolTipText("Hello"); // Enable tooltips
        }
        @Override
        public JToolTip createToolTip() {
            JToolTip tooltip = super.createToolTip();
            tooltip.setBackground(Color.YELLOW);
            tooltip.setForeground(Color.BLACK);
            return tooltip;
        }

        @Override
        public Dimension getPreferredSize() {
            // Set the preferred size to the maximum extent of your map
    
            return new Dimension(WIDTH + 50, HEIGHT + 50); // Add some extra space for better visualization
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            // Draw cities
            for (Map.Entry<String, City> cityEntry : citiesDS.entrySet()) {

                City city = cityEntry.getValue();
                float x = convertCoords(city.lngt, "lngt");
                float y  = convertCoords(city.lat, "lat");

                Color color = Color.BLUE;
                if (StringStandardize.standardizeString(city.state).equals("indiana")) color = Color.MAGENTA;
                if (StringStandardize.standardizeString(city.state).equals("northdakota")) color = Color.CYAN;
                if (StringStandardize.standardizeString(city.state).equals("southdakota")) color = Color.DARK_GRAY;
                if (StringStandardize.standardizeString(city.state).equals("nebraska")) color = Color.LIGHT_GRAY;
                if (StringStandardize.standardizeString(city.state).equals("kansas")) color = Color.ORANGE;
                if (StringStandardize.standardizeString(city.state).equals("oklahoma")) color = Color.PINK;
                if (StringStandardize.standardizeString(city.state).equals("texas")) color = Color.WHITE;
                if (StringStandardize.standardizeString(city.state).equals("loisiana")) color = Color.YELLOW;
                if (StringStandardize.standardizeString(city.state).equals("alamba")) color = Color.GREEN;
                if (StringStandardize.standardizeString(city.state).equals("tennessee")) color = new Color(0.2f, 0.7f, 0.33f);
                if (StringStandardize.standardizeString(city.state).equals("arkansas")) color = new Color(0.3f, 0.2f, 0.6f);
                if (StringStandardize.standardizeString(city.state).equals("missouri")) color = new Color(0.9f, 0.1f, 0.3f);
                if (StringStandardize.standardizeString(city.state).equals("illinois")) color = new Color(0.5f, 0.0f, 0.5f);
                if (StringStandardize.standardizeString(city.state).equals("wisconsin")) color = new Color(0.3f, 0.9f, 0.9f);;
                if (StringStandardize.standardizeString(city.state).equals("iowa")) color = new Color(0.4f, 0.4f, 0.2f);
                if (StringStandardize.standardizeString(city.state).equals("minnesota")) color = new Color(0.1f, 0.1f, 0.5f);


                g2d.setColor(color);
                g2d.fill(new Ellipse2D.Double(x - 10, y - 10, 20, 20));
                g2d.setColor(Color.BLACK);
                g2d.drawString(city.name, x, y - 10);

                for (CityConnectionStruct ccs : city.connections){
                        String connKey = StringStandardize.standardizeString(ccs.state) + "__" + StringStandardize.standardizeString(ccs.name);
                        City connCity = citiesDS.get(connKey);
                        if (connCity != null){
                            float conn_x = convertCoords(connCity.lngt, "lngt") ;
                            float conn_y  = convertCoords(connCity.lat, "lat");

                            double distance = ccs.distance;
                            g2d.setColor(Color.RED);
                            g2d.drawLine((int)x, (int)y, (int)conn_x, (int)conn_y);
                            g2d.setColor(Color.BLACK);
                            // g2d.drawString(String.format("%.2f", distance), (x + conn_x) / 2, (y + conn_y) / 2);
                        }
                }
            }
        }
    }

    private boolean isPointOnLine(Point p, Point start, Point end) {
        double d1 = distance(start, p);
        double d2 = distance(p, end);
        double lineLength = distance(start, end);
        return Math.abs(d1 + d2 - lineLength) < 1;
    }

    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    public static void main(String[] args) {


        String citiesCsvPath = "Cities_2.txt" ;
        String connectionCsvPath = "Connections_3.txt" ;

        try{
            Map<String, String[]> weatherData = LDP.loadIndividualCityData(citiesCsvPath);
            Map<String, String[]> connectionData = LDP.loadCityConnectionData(connectionCsvPath);
            HashMap<String, City> citiesDS = BuildCityObjects.twoWayBuild(weatherData, connectionData);
            SwingUtilities.invokeLater(() -> new MapVisualize(citiesDS));

        }
        catch(Exception e){

        }
    }
}
