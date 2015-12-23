package de.devmil.paperlaunch.model;

import java.util.List;

public interface IFolder extends IEntry {
    List<IEntry> getSubEntries();
    void setSubEntries(List<IEntry> entries);
}
