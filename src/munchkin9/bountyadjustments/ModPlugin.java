package munchkin9.bountyadjustments;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import org.json.JSONObject;

import java.awt.Color;

public class ModPlugin extends BaseModPlugin {
    public static final String
        ID = "munchkin9_bounty_adjustments",
        SETTINGS_PATH = "BountyAdjustmentsConfig.ini",
        SYSTEM_BOUNTY_KEY = "baseSystemBounty",
        CUSTOM_BOUNTY_KEY = "baseCustomBounty",
        CUSTOM_LEVEL_KEY = "personCustomBountyPerLevel",
        PERSON_BOUNTY_KEY = "basePersonBounty",
        PERSON_LEVEL_KEY = "personBountyPerLevel",
        PIRATEBASE_BOUNTY_KEY = "pirateBaseBounty",
        LUDDIC_SMALL_BOUNTY_KEY = "luddicPathBaseBountySmall",
        LUDDIC_LARGE_BOUNTY_KEY = "luddicPathBaseBountyLarge";

    public static float
        SYSTEM_ORIGINAL = 1000,
        CUSTOM_ORIGINAL = 70000,
        CUSTOM_LEVEL_ORIGINAL = 25000,
        PERSON_ORIGINAL = 40000,
        PERSON_LEVEL_ORIGINAL = 15000,
        PIRATEBASE_ORIGINAL = 40000,
        LUDDIC_SMALL_ORIGINAL = 150000,
        LUDDIC_LARGE_ORIGINAL = 300000,

        SYSTEM_MULTIPLIER = 1.0f,
        CUSTOM_MULTIPLIER = 1.0f,
        CUSTOM_LEVEL_MULTIPLER = 1.0f,
        PERSON_MULTIPLIER = 1.0f,
        PERSON_LEVEL_MULTIPLIER = 1.0f,
        PIRATEBASE_MULTIPLIER = 1.0f,
        LUDDIC_MULTIPLIER = 1.0f;

    static boolean settingsAreRead = false;

    class Version {
        public final int MAJOR, MINOR, PATCH, RC;

        public Version(String versionStr) {
            String[] temp = versionStr.replace("Starsector ", "").replace("a", "").split("-RC");

            RC = temp.length > 1 ? Integer.parseInt(temp[1]) : 0;

            temp = temp[0].split("\\.");

            MAJOR = temp.length > 0 ? Integer.parseInt(temp[0]) : 0;
            MINOR = temp.length > 1 ? Integer.parseInt(temp[1]) : 0;
            PATCH = temp.length > 2 ? Integer.parseInt(temp[2]) : 0;
        }

        public boolean isOlderThan(Version other, boolean ignoreRC) {
            if(MAJOR < other.MAJOR) return true;
            if(MINOR < other.MINOR) return true;
            if(PATCH < other.PATCH) return true;
            if(!ignoreRC && !other.isOlderThan(this, true) && RC < other.RC) return true;

            return false;
        }

        @Override
        public String toString() {
            return String.format("%d.%d.%d%s-RC%d", MAJOR, MINOR, PATCH, (MAJOR >= 1 ? "" : "a"), RC);
        }
    }

    @Override
    public void onApplicationLoad() throws Exception {
        String message = "";

        try {
            ModSpecAPI spec = Global.getSettings().getModManager().getModSpec(ID);
            Version minimumVersion = new Version(spec.getGameVersion());
            Version currentVersion = new Version(Global.getSettings().getVersionString());

            if(currentVersion.isOlderThan(minimumVersion, false)) {
                message = String.format("\rThis version of Starsector is too old for %s!" +
                                "\rPlease make sure Starsector is up to date. (http://fractalsoftworks.com/preorder/)" +
                                "\rMinimum Version: %s" +
                                "\rCurrent Version: %s",
                        spec.getName(), minimumVersion, currentVersion);
            }

            SYSTEM_ORIGINAL = Global.getSettings().getFloat(SYSTEM_BOUNTY_KEY);
            CUSTOM_ORIGINAL = Global.getSettings().getFloat(CUSTOM_BOUNTY_KEY);
            CUSTOM_LEVEL_ORIGINAL = Global.getSettings().getFloat(CUSTOM_LEVEL_KEY);
            PERSON_ORIGINAL = Global.getSettings().getFloat(PERSON_BOUNTY_KEY);
            PERSON_LEVEL_ORIGINAL = Global.getSettings().getFloat(PERSON_LEVEL_KEY);
            PIRATEBASE_ORIGINAL = Global.getSettings().getFloat(PIRATEBASE_BOUNTY_KEY + "1");
            LUDDIC_SMALL_ORIGINAL = Global.getSettings().getFloat(LUDDIC_SMALL_BOUNTY_KEY);
            LUDDIC_LARGE_ORIGINAL = Global.getSettings().getFloat(LUDDIC_LARGE_BOUNTY_KEY);

            readSettingsIfNecessary(true);
        } catch (Exception e) {
            Global.getLogger(this.getClass()).error("Version comparison failed.", e);
        }

        if(!message.isEmpty()) throw new Exception(message);
    }

    static boolean readSettingsIfNecessary(boolean forceRefresh) {
        try {
            if(forceRefresh) settingsAreRead = false;

            if(settingsAreRead) return true;

            JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);

            SYSTEM_MULTIPLIER = (float)cfg.getDouble("systemBountyMultiplier");
            CUSTOM_MULTIPLIER = (float)cfg.getDouble("customBaseBountyMultiplier");
            CUSTOM_LEVEL_MULTIPLER = (float)cfg.getDouble("customBountyPerLevelMultiplier");
            PERSON_MULTIPLIER = (float)cfg.getDouble("personBaseBountyMultiplier");
            PERSON_LEVEL_MULTIPLIER = (float)cfg.getDouble("personPerLevelMultiplier");
            PIRATEBASE_MULTIPLIER = (float)cfg.getDouble("pirateStationBountyMultiplier");
            LUDDIC_MULTIPLIER = (float)cfg.getDouble("luddicStationBountyMultiplier");

            Global.getSettings().setFloat(SYSTEM_BOUNTY_KEY, SYSTEM_ORIGINAL * SYSTEM_MULTIPLIER);
            Global.getSettings().setFloat(CUSTOM_BOUNTY_KEY, CUSTOM_ORIGINAL * CUSTOM_MULTIPLIER);
            Global.getSettings().setFloat(CUSTOM_LEVEL_KEY, CUSTOM_LEVEL_ORIGINAL * CUSTOM_LEVEL_MULTIPLER);
            Global.getSettings().setFloat(PERSON_BOUNTY_KEY, PERSON_ORIGINAL * PERSON_MULTIPLIER);
            Global.getSettings().setFloat(PERSON_LEVEL_KEY, PERSON_LEVEL_ORIGINAL * PERSON_LEVEL_MULTIPLIER);

            for(int i = 1; i <= 5; i++) {
                Global.getSettings().setFloat(PIRATEBASE_BOUNTY_KEY + i, PIRATEBASE_ORIGINAL * i * PIRATEBASE_MULTIPLIER);
            }

            Global.getSettings().setFloat(LUDDIC_SMALL_BOUNTY_KEY, LUDDIC_SMALL_ORIGINAL * LUDDIC_MULTIPLIER);
            Global.getSettings().setFloat(LUDDIC_LARGE_BOUNTY_KEY, LUDDIC_LARGE_ORIGINAL * LUDDIC_MULTIPLIER);

            return settingsAreRead = true;
        } catch (Exception e) {
            return settingsAreRead = reportCrash(e);
        }
    }

    public static boolean reportCrash(Exception exception) {
        return reportCrash(exception, true);
    }
    public static boolean reportCrash(Exception exception, boolean displayToUser) {
        try {
            String stackTrace = "", message = "BountyAdjustments encountered an error!\nPlease let the mod author know.";

            for(int i = 0; i < exception.getStackTrace().length; i++) {
                StackTraceElement ste = exception.getStackTrace()[i];
                stackTrace += "    " + ste.toString() + System.lineSeparator();
            }

            Global.getLogger(ModPlugin.class).error(exception.getMessage() + System.lineSeparator() + stackTrace);

            if (!displayToUser) {
                return true;
            } else if (Global.getSector() != null) {
                CampaignUIAPI ui = Global.getSector().getCampaignUI();

                ui.addMessage(message, Color.RED);
                ui.addMessage(exception.getMessage(), Color.ORANGE);
                ui.showConfirmDialog(message + "\n\n" + exception.getMessage(), "Ok", null, null, null);

                if(ui.getCurrentInteractionDialog() != null) ui.getCurrentInteractionDialog().dismiss();
            } else return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
