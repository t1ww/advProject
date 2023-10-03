package se233.advproject;

import javafx.scene.control.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Data {
    private static final Data instance = new Data();
    public static Data getInstance(){
        return instance;
    }
    private List<File> files = new ArrayList();
    private ListView listView;
    private int listViewSelecting;

    public int getListViewSelecting() {
        return listViewSelecting;
    }

    public void setListViewSelecting(int listViewSelecting) {
        this.listViewSelecting = listViewSelecting;
    }

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public List<File> getFiles() { return files; }
    public void addFiles(List<File> f) {
        if(files == null) {
            setFiles(f);
        }else files.addAll(f);
    }
    public void addFiles(File f) {
        files.add(f);
    }
    public void setFiles(List<File> files) {
        this.files = files;
    }
    Data(){}
}
