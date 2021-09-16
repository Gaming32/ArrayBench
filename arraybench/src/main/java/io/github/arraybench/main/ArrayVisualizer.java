package io.github.arraybench.main;

import io.github.arraybench.utils.Delays;
import io.github.arraybench.utils.Highlights;
import io.github.arraybench.utils.Reads;
import io.github.arraybench.utils.Writes;

final public class ArrayVisualizer {
    private int length;

    private Delays Delays;
    private Highlights Highlights;
    private Reads Reads;
    private Writes Writes;

    public ArrayVisualizer(int length) {
        this.length = length;

        this.Delays = new Delays();
        this.Highlights = new Highlights();
        this.Reads = new Reads();
        this.Writes = new Writes();
    }

    public Delays getDelays() {
        return Delays;
    }

    public Highlights getHighlights() {
        return Highlights;
    }

    public Reads getReads() {
        return this.Reads;
    }

    public Writes getWrites() {
        return this.Writes;
    }

    public int getCurrentLength() {
        return this.length;
    }

    public void setCurrentLength(int length) {
        this.length = length;
    }
}
