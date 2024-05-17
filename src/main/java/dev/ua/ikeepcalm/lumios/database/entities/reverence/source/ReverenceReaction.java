package dev.ua.ikeepcalm.lumios.database.entities.reverence.source;

public enum ReverenceReaction {

    LIKE {
        @Override
        public String toString() {
            return "👍";
        }
    },
    DISLIKE {
        @Override
        public String toString() {
            return "👎";
        }
    },

    LOVE {
        @Override
        public String toString() {
            return "❤";
        }
    },
    HEART {
        @Override
        public String toString() {
            return "❤‍🔥";
        }
    },
    FIRE {
        @Override
        public String toString() {
            return "🔥";
        }
    },
    DEVIL {
        @Override
        public String toString() {
            return "😈";
        }
    },
    UFO {
        @Override
        public String toString() {
            return "👾";
        }
    },
    PUMPKIN {
        @Override
        public String toString() {
            return "🎃";
        }
    },
    STRAWBERRY {
        @Override
        public String toString() {
            return "🍓";
        }
    },
    EWW {
        @Override
        public String toString() {
            return "🤮";
        }
    },
    BANANA {
        @Override
        public String toString() {
            return "🍌";
        }
    },

    ANGRY {
        @Override
        public String toString() {
            return "🤬";
        }
    },
    CLOWN {
        @Override
        public String toString() {
            return "🤡";
        }
    },
    NERD {
        @Override
        public String toString() {
            return "🤓";
        }
    },
    SLAYER {
        @Override
        public String toString() {
            return "💅";
        }
    },
    LIGHTNING {
        @Override
        public String toString() {
            return "⚡";
        }
    },
    OMG {
        @Override
        public String toString() {
            return "😱";
        }
    },
    LAUGH {
        @Override
        public String toString() {
            return "😂";
        }
    },
    CRY {
        @Override
        public String toString() {
            return "😭";
        }
    },
    UNICORN {
        @Override
        public String toString() {
            return "🦄";
        }
    },
    WOW {
        @Override
        public String toString() {
            return "😮";
        }
    },
    SAD {
        @Override
        public String toString() {
            return "😢";
        }
    },
    HUNDRED {
        @Override
        public String toString() {
            return "💯";
        }
    },
    FUCK {
        @Override
        public String toString() {
            return "🖕";
        }
    },
    WHALE {
        @Override
        public String toString() {
            return "🐳";
        }
    },

    PREMIUM,

    DEFAULT;

    public static ReverenceReaction determineReaction(String reaction) {
        return switch (reaction) {
            case "👍" -> LIKE;
            case "👎" -> DISLIKE;
            case "❤" -> HEART;
            case "😈" -> DEVIL;
            case "👾" -> UFO;
            case "🎃" -> PUMPKIN;
            case "🍓" -> STRAWBERRY;
            case "🤬" -> ANGRY;
            case "🤡" -> CLOWN;
            case "🤓" -> NERD;
            case "💅" -> SLAYER;
            case "⚡" -> LIGHTNING;
            case "😱" -> OMG;
            case "😂" -> LAUGH;
            case "😭" -> CRY;
            case "😮" -> WOW;
            case "😢" -> SAD;
            case "🐳" -> WHALE;
            case "🔥" -> FIRE;
            case "🖕" -> FUCK;
            case "💯" -> HUNDRED;
            case "🤮" -> EWW;
            case "🍌" -> BANANA;
            default -> DEFAULT;
        };
    }

    public static int determineReactionValue(ReverenceReaction reaction) {
        return switch (reaction) {
            case LIKE, WHALE, HUNDRED, WOW, UNICORN, LAUGH, LIGHTNING, BANANA, STRAWBERRY, PUMPKIN, UFO, DEVIL -> 5;
            case DISLIKE, SLAYER, ANGRY, EWW -> -5;
            case LOVE, PREMIUM, FIRE, HEART -> 10;
            case CLOWN, FUCK -> -10;
            case NERD -> -7;
            case OMG, SAD -> 1;
            case CRY -> 2;
            case DEFAULT -> 0;
        };
    }

}
