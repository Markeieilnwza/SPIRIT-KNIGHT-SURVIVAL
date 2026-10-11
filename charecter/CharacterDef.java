package charecter;

import java.util.List;
import java.util.function.Supplier;
import skill.Skill;

public class CharacterDef {
    public static final List<CharacterDef> ALL = List.of(
            new KnightDef(), new MageDef(), new PriestDef());

    private final String name;
    private final String spriteFile;
    private final double maxHp;
    private final double speed;
    private final String skillDescription;
    private final Supplier<Skill> skillFactory;

    public CharacterDef(String name, String spriteFile, double maxHp, double speed,
            String skillDescription, Supplier<Skill> skillFactory) {
        this.name = name;
        this.spriteFile = spriteFile;
        this.maxHp = maxHp;
        this.speed = speed;
        this.skillDescription = skillDescription;
        this.skillFactory = skillFactory;
    }

    public String getName() {
        return name;
    }

    public String getSpriteFile() {
        return spriteFile;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public double getSpeed() {
        return speed;
    }

    public String getSkillDescription() {
        return skillDescription;
    }

    public Skill createSkill() {
        return skillFactory.get();
    }
}
