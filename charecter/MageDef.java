package charecter;

import skill.MissileSkill;

public final class MageDef extends CharacterDef {
    public MageDef() {
        super("Mage", "mageright.gif", 90.0, 205.0, "Fires missiles at enemies", MissileSkill::new);
    }
}
