package androidsamples.java.tictactoe;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserInfo {
    private String userId;
    private Integer wins;
    private Integer losses;
    private String email;

    public UserInfo() {
    }

    public UserInfo(String userId, Integer wins, Integer losses,String email) {
        this.userId = userId;
        this.wins = wins;
        this.losses = losses;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getLosses() {
        return losses;
    }

    public Integer getWins() {
        return wins;
    }

    public String getEmail() {
        return email;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", wins=" + wins +
                ", losses=" + losses +
                ", email='" + email + '\'' +
                '}';
    }
}
