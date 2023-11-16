# CS 50510 - Project

## Installation

Make sure to have a jdk installed in your OS platform. That's it :)
The dependencies are packaged as .jar files already

## Usage

The program is used for parsing the input data and creating structure. Finally, an A* algorithm will inform of the optimal path

- For validity of input data, please run the [LDP.java](LDP.java) file with aptly named input files for **city** data & **city connections** data. Please chnage the input files and flags accordinly. I recommend to leave **flags to default**. However you can experiment around wtih flags if you wish. It runs basic **formatting checks, csv header checks and numeric data checks**
- For verification, please run [LDP.java](LDP.java) as well. It also includes **Member contribution function/check** . More **importantly**, it includes verification by using **BFS** to see if a path exists between any pair of city.

The program should provide logs if it correctly parses and data. It will provide detailed logs in a log file in case of erroneous formatting or invalid data

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

[MIT](https://choosealicense.com/licenses/mit/)