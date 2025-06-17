package ludo.mentis.aciem.controlserver.model;

/**
 * Class to represent file information
 */
public record FileInfo(String name, boolean directory, long size, long lastModified) {
}
