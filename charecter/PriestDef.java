package charecter;

import skill.NovaSkill;

public final class PriestDef extends CharacterDef {
    public PriestDef() {
        super("Frost", "frost.gif", 110.0, 185.0, "Holy waves", NovaSkill::new);
    }
}
