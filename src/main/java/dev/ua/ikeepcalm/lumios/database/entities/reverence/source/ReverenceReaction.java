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
            case LIKE -> 5;
            case DISLIKE -> -5;
            case LOVE -> 10;
            case HEART -> 10;
            case FIRE -> 10;
            case DEVIL -> 5;
            case UFO -> 5;
            case PUMPKIN -> 5;
            case STRAWBERRY -> 5;
            case EWW -> -5;
            case BANANA -> 5;
            case ANGRY -> -5;
            case CLOWN -> -10;
            case NERD -> -7;
            case SLAYER -> -5;
            case LIGHTNING -> 5;
            case OMG -> 1;
            case LAUGH -> 5;
            case CRY -> 2;
            case UNICORN -> 5;
            case WOW -> 5;
            case SAD -> 1;
            case HUNDRED -> 5;
            case FUCK -> -10;
            case WHALE -> 5;
            case PREMIUM -> 10;
            case DEFAULT -> 0;
        };
    }

}
