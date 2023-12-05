package src;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;


public class MapVisualizeDetailsETA extends JFrame {
    private float min_x = -105;
    private float max_x = -85;
    private float min_y = 23;
    private float max_y = 49;

    private int WIDTH = 1500;
    private int HEIGHT = 1000;
    private static final int    EARTH_RADIUS    = 1;
    private static final double FOCAL_LENGTH    = 500; 

        double convertXY(double lat, double lngt, String type){
            double latitude = lat * Math.PI / 180;
            double longitude = lngt * Math.PI / 180;
        
            double x = EARTH_RADIUS * (Math.sin(latitude)) * (Math.cos(longitude));
            double y = EARTH_RADIUS * (Math.sin(latitude)) * (Math.sin(longitude));
            double z = EARTH_RADIUS * Math.cos(latitude);
        
            double projectedX = x * FOCAL_LENGTH / (FOCAL_LENGTH + z) *2* WIDTH + WIDTH;
            double projectedY = y * FOCAL_LENGTH / (FOCAL_LENGTH + z) * 1.5*HEIGHT + 1.5*HEIGHT;
            System.out.println(projectedX);
                        System.out.println(projectedY);

            if (type.equals("lat")) return projectedY;
            else if (type.equals("lngt")) return projectedX;
            return -1;
        }


        private float convertCoords(float val, String type){
            if (type.equals("lngt")) return (val - min_x)/(max_x-min_x) * WIDTH;
            else if (type.equals("lat")) return HEIGHT - (val - min_y)/(max_y-min_y) * HEIGHT;
            return -1;
        }

        public MapVisualizeDetailsETA(HashMap<String, City> citiesDS, Map<String, ArrayList<Integer>> weatherRiskMap, WrapperOutput path) {
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
                                String connCityKey = utils.standardizeCityKey(ccs.state, ccs.name);
                                // String connCityKey = StringStandardize.standardizeString(ccs.state) + "__" + StringStandardize.standardizeString(ccs.name);
                                City connCity = citiesDS.get(connCityKey);
                                if (connCity != null & (path.cityKeys.indexOf(connCityKey) - path.cityKeys.indexOf(cityNameKey)) == 1){
                                    float conn_x = convertCoords(connCity.lngt, "lngt") ;
                                    float conn_y  = convertCoords(connCity.lat, "lat");

                                    Point point2 = new Point((int)conn_x, (int)conn_y) ;
                                    if (isPointOnLine(e.getPoint(), point1, point2)) {
                                        double distance = ccs.distance / 1.6;
                                        long startTime = path.gScore.get(cityNameKey).startTime;
                                        Date date=new Date(startTime);
                                        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy HH:mm");
                                        String dateText = df2.format(date);
                                        CostStruct c = new CostStruct(0,0, startTime).add(OptimalPath.costBetweenCities(cityNameKey, connCityKey, citiesDS, weatherRiskMap, startTime));
                                        String b = String.format("<html> Distance : " + distance + " miles <br>" +
                                        "Start time = " + dateText + " <br>" +
                                        " Expected time taken = " + ((c.startTime - startTime) * 1.0 /60000) + " min <br>" +
                                        " Weather : " + WeatherParse.getNearestWeather(startTime, city.weatherData) + " <br>" +
                                        " Weather risk " + c.risk + " <br>" +
                                        " Gas Consumption : " + String.format("%.2f",(distance / (c.mileage * 3.78541/1.6))) + " gallons <br> </html>" ); 
                                        mapPanel.setToolTipText(b);
                                        System.out.println(b);
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
                if (utils.standardizeString(city.state).equals("indiana")) color = Color.MAGENTA;
                if (utils.standardizeString(city.state).equals("northdakota")) color = Color.CYAN;
                if (utils.standardizeString(city.state).equals("southdakota")) color = Color.DARK_GRAY;
                if (utils.standardizeString(city.state).equals("nebraska")) color = Color.LIGHT_GRAY;
                if (utils.standardizeString(city.state).equals("kansas")) color = Color.ORANGE;
                if (utils.standardizeString(city.state).equals("oklahoma")) color = Color.PINK;
                if (utils.standardizeString(city.state).equals("texas")) color = Color.WHITE;
                if (utils.standardizeString(city.state).equals("loisiana")) color = Color.YELLOW;
                if (utils.standardizeString(city.state).equals("alamba")) color = Color.GREEN;
                if (utils.standardizeString(city.state).equals("tennessee")) color = new Color(0.2f, 0.7f, 0.33f);
                if (utils.standardizeString(city.state).equals("arkansas")) color = new Color(0.3f, 0.2f, 0.6f);
                if (utils.standardizeString(city.state).equals("missouri")) color = new Color(0.9f, 0.1f, 0.3f);
                if (utils.standardizeString(city.state).equals("illinois")) color = new Color(0.5f, 0.0f, 0.5f);
                if (utils.standardizeString(city.state).equals("wisconsin")) color = new Color(0.3f, 0.9f, 0.9f);;
                if (utils.standardizeString(city.state).equals("iowa")) color = new Color(0.4f, 0.4f, 0.2f);
                if (utils.standardizeString(city.state).equals("minnesota")) color = new Color(0.1f, 0.1f, 0.5f);


                g2d.setColor(color);
                g2d.fill(new Ellipse2D.Double(x - 10, y - 10, 20, 20));
                g2d.setColor(Color.BLACK);
                g2d.drawString(city.name, x, y - 10);

                for (CityConnectionStruct ccs : city.connections){
                        String connKey = utils.standardizeCityKey(ccs.state, ccs.name);
                        // String connKey = StringStandardize.standardizeString(ccs.state) + "__" + StringStandardize.standardizeString(ccs.name);
                        City connCity = citiesDS.get(connKey);
                        if (connCity != null){
                            float conn_x = convertCoords(connCity.lngt, "lngt") ;
                            float conn_y  = convertCoords(connCity.lat, "lat");

                            double distance = ccs.distance;
                            g2d.setColor(Color.RED);
                            g2d.drawLine((int)x, (int)y, (int)conn_x, (int)conn_y);
                            g2d.setColor(Color.BLACK);
                            g2d.drawString(String.format("%.2f", distance /1.6), (x + conn_x) / 2, (y + conn_y) / 2);
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

    // public static void main(String[] args) {


    //     String citiesCsvPath = "data/Cities_3.txt" ;
    //     String connectionCsvPath = "data/Connections_4.txt" ;
    //     String weatherRiskCsvPath = "data/Weather Risk Factor.txt";


    //     try{
    //         Map<String, String[]> weatherData = LDP.loadIndividualCityData(citiesCsvPath);
    //         Map<String, String[]> connectionData = LDP.loadCityConnectionData(connectionCsvPath);
    //         HashMap<String, City> citiesDS = BuildCityObjects.twoWayBuild(weatherData, connectionData);
    //         Map<String, ArrayList<Integer>> weatherRiskMap = OptimalPath.loadWeatherRiskFactor(weatherRiskCsvPath);
    //         long startTime = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("11/09/2023 12:22").getTime();
    //         SwingUtilities.invokeLater(() -> new MapVisualizeDetails(citiesDS, weatherRiskMap, startTime));


    //     }
    //     catch(Exception e){

    //     }
    // }
}
