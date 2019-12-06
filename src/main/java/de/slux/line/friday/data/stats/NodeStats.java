package de.slux.line.friday.data.stats;

/**
 * @author slux
 */
public class NodeStats {

    private String champ;
    private int occurrences;
    private int totalDeaths;
    private int deathItems;
    private int node;

    /**
     * Ctor
     *
     * @param node
     * @param champ
     * @param occurrences
     * @param totalDeaths
     * @param deathItems
     */
    public NodeStats(int node, int occurrences, int totalDeaths, int deathItems) {

        this.champ = null;
        this.occurrences = occurrences;
        this.totalDeaths = totalDeaths;
        this.deathItems = deathItems;
    }

    /**
     * Ctor
     *
     * @param champ
     * @param occurrences
     * @param totalDeaths
     * @param deathItems
     */
    public NodeStats(String champ, int occurrences, int totalDeaths, int deathItems) {
        this(-1, occurrences, totalDeaths, deathItems);
        this.champ = champ;
    }

    /**
     * @return the champ
     */
    public String getChamp() {
        return champ;
    }

    /**
     * @param champ the champ to set
     */
    public void setChamp(String champ) {
        this.champ = champ;
    }

    /**
     * @return the occurrences
     */
    public int getOccurrences() {
        return occurrences;
    }

    /**
     * @param occurrences the occurrences to set
     */
    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    /**
     * @return the totalDeaths
     */
    public int getTotalDeaths() {
        return totalDeaths;
    }

    /**
     * @param totalDeaths the totalDeaths to set
     */
    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    /**
     * @return the deathItems
     */
    public int getDeathItems() {
        return deathItems;
    }

    /**
     * @param deathItems the deathItems to set
     */
    public void setDeathItems(int deathItems) {
        this.deathItems = deathItems;
    }

    /**
     * @return the node
     */
    public int getNode() {
        return node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(int node) {
        this.node = node;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NodeStats [node=" + node + ", champ=" + champ + ", occurrences=" + occurrences + ", totalDeaths="
                + totalDeaths + ", deathItems=" + deathItems + "]";
    }

}
