package timetracker.gui;

import timetracker.API.DataReader;

public class Application{

    public Application() {
        MainForm mainForm = new MainForm();
    }

    private void collect() {
        DataReader dataReader = new DataReader();
        dataReader.readInAllToGlobal();
        dataReader.close();
    }

}