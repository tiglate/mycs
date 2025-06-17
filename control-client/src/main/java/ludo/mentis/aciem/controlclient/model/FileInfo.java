package ludo.mentis.aciem.controlclient.model;

/**
 * Class to represent file information
 * This is a mirror of the FileInfo class in the control-server
 */
public record FileInfo(String name, boolean directory, long size, long lastModified) {
}