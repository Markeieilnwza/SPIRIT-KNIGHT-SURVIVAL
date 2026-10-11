package charecter;

import skill.OrbitSkill;

public final class KnightDef extends CharacterDef {
    public KnightDef() {
        super("Knight", "knightright.gif", 140.0, 190.0, "Orbiting swords", OrbitSkill::new);
    }
}
