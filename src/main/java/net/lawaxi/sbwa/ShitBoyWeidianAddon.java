package net.lawaxi.sbwa;

import net.lawaxi.Shitboy;
import net.lawaxi.sbwa.config.ConfigConfig;
import net.lawaxi.sbwa.util.Common;
import net.mamoe.mirai.console.plugin.Plugin;
import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public final class ShitBoyWeidianAddon extends JavaPlugin {
    public static final ShitBoyWeidianAddon INSTANCE = new ShitBoyWeidianAddon();
    public static Shitboy INSTANCE_SHITBOY;
    public static ConfigConfig config;

    private ShitBoyWeidianAddon() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.shitboyWA", "0.1.0-beta1")
                .name("ShitBoyWeidianAddon")
                .author("delay")
                .dependsOn("net.lawaxi.shitboy", false)
                .build());
    }

    @Override
    public void onEnable() {
        initConfig();
        if (loadShitboy()) {
            INSTANCE_SHITBOY.handlerWeidianSender = new NewHandler();
        }
        getLogger().info("Plugin loaded!");
    }

    private void initConfig() {
        new Common(getConfigFolder());
        config = new ConfigConfig(resolveConfigFile("config.setting"));
    }

    private boolean loadShitboy() {
        for (Plugin plugin : PluginManager.INSTANCE.getPlugins()) {
            if (plugin instanceof Shitboy) {
                INSTANCE_SHITBOY = (Shitboy) plugin;
                getLogger().info("读取Shitboy插件成功");
                return true;
            }
        }
        getLogger().info("读取Shitboy插件失败");
        return false;
    }
}