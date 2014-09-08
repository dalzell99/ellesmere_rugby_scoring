package chris.myapplication;

class ScoringPlay {

    int minutes;
    String play;
    String description;

    ScoringPlay(int minutes, String play, String description) {
        this.minutes = minutes;
        this.play = play;
        this.description = description;
    }

    public int getMinutes() {
        return minutes;
    }
    public String getPlay() {
        return play;
    }
    public String getDescription() {
        return description;
    }
}