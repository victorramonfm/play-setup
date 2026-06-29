package org.play.domain.models;

import java.util.ArrayList;
import java.util.List;

public class HumanPlayer extends Player {
    private static final long serialVersionUID = 1L;
    private final String nickname;

    public HumanPlayer(String id, String name, String nickname) {
        super(id, name, new ArrayList<>());
        this.nickname = nickname;
    }

    public HumanPlayer(String id, String name, String nickname, List<String> opponents) {
        super(id, name, opponents);
        this.nickname = nickname;
    }

    public String getNickname() { return nickname; }

    @Override
    public String getPlayerType() { return "Humano (" + nickname + ")"; }

    @Override
    public int getTiebreakerPriority() { return 2; }
}