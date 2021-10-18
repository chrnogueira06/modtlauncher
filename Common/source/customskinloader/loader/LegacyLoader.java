package customskinloader.loader;

import java.io.File;

import com.mojang.util.UUIDTypeAdapter;
import org.apache.commons.lang3.StringUtils;

import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpRequestUtil.HttpRequest;
import customskinloader.utils.HttpRequestUtil.HttpResponce;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

import javax.annotation.Nonnull;

public class LegacyLoader implements ProfileLoader.IProfileLoader {
    public static final String USERNAME_PLACEHOLDER = "{USERNAME}";
    public static final String UUID_PLACEHOLDER = "{UUID}";

    @Override
    public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
        String username = gameProfile.getName();
        UserProfile profile = new UserProfile();
        if (StringUtils.isNoneEmpty(ssp.skin)) {
            String skin = expandURL(ssp.skin, username);
            if (HttpUtil0.isLocal(ssp.skin)) {
                File skinFile = new File(CustomSkinLoader.DATA_DIR, skin);
                if (skinFile.exists() && skinFile.isFile())
                    profile.skinUrl = HttpTextureUtil.getLocalLegacyFakeUrl(skin, HttpTextureUtil.getHash(skin, skinFile.length(), skinFile.lastModified()));
            } else {
                HttpResponce responce = HttpRequestUtil.makeHttpRequest(new HttpRequest(skin).setUserAgent(ssp.userAgent).setCheckPNG(ssp.checkPNG != null && ssp.checkPNG).setLoadContent(false).setCacheTime(90));
                if (responce.success)
                    profile.skinUrl = HttpTextureUtil.getLegacyFakeUrl(skin);
            }
            profile.model = profile.hasSkinUrl() ? ssp.model : null;
        }
        if (StringUtils.isNoneEmpty(ssp.cape)) {
            String cape = expandURL(ssp.cape, username);
            if (HttpUtil0.isLocal(ssp.cape)) {
                File capeFile = new File(CustomSkinLoader.DATA_DIR, cape);
                if (capeFile.exists() && capeFile.isFile())
                    profile.capeUrl = HttpTextureUtil.getLocalLegacyFakeUrl(cape, HttpTextureUtil.getHash(cape, capeFile.length(), capeFile.lastModified()));
            } else {
                HttpResponce responce = HttpRequestUtil.makeHttpRequest(new HttpRequest(cape).setUserAgent(ssp.userAgent).setCheckPNG(ssp.checkPNG != null && ssp.checkPNG).setLoadContent(false).setCacheTime(90));
                if (responce.success)
                    profile.capeUrl = HttpTextureUtil.getLegacyFakeUrl(cape);
            }
        }
        if (ModelManager0.isElytraSupported() && StringUtils.isNoneEmpty(ssp.elytra)) {
            String elytra = expandURL(ssp.elytra, username);
            if (HttpUtil0.isLocal(ssp.elytra)) {
                File elytraFile = new File(CustomSkinLoader.DATA_DIR, elytra);
                if (elytraFile.exists() && elytraFile.isFile())
                    profile.elytraUrl = HttpTextureUtil.getLocalLegacyFakeUrl(elytra, HttpTextureUtil.getHash(elytra, elytraFile.length(), elytraFile.lastModified()));
            } else {
                HttpResponce responce = HttpRequestUtil.makeHttpRequest(new HttpRequest(elytra).setUserAgent(ssp.userAgent).setCheckPNG(ssp.checkPNG != null && ssp.checkPNG).setLoadContent(false).setCacheTime(90));
                if (responce.success)
                    profile.elytraUrl = HttpTextureUtil.getLegacyFakeUrl(elytra);
            }
        }
        if (profile.isEmpty()) {
            CustomSkinLoader.logger.info("Both skin and cape not found.");
            return null;
        }
        return profile;
    }

    @Override
    public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
        return (!StringUtils.isNoneEmpty(ssp0.skin) || ssp0.skin.equalsIgnoreCase(ssp1.skin)) || (!StringUtils.isNoneEmpty(ssp0.cape) || ssp0.cape.equalsIgnoreCase(ssp1.cape));
    }

    @Override
    public String getName() {
        return "Legacy";
    }

    @Override
    public void init(SkinSiteProfile ssp) {
        if (HttpUtil0.isLocal(ssp.skin))
            initFolder(ssp.skin);
        if (HttpUtil0.isLocal(ssp.cape))
            initFolder(ssp.cape);
        if (HttpUtil0.isLocal(ssp.elytra))
            initFolder(ssp.elytra);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initFolder(String target) {
        String file = target.replace(USERNAME_PLACEHOLDER, "init");
        File folder = new File(CustomSkinLoader.DATA_DIR, file).getParentFile();
        if (folder != null && !folder.exists())
            folder.mkdirs();
    }

    private String expandURL(String url, String username) {
        String t = url.replace(USERNAME_PLACEHOLDER, username);
        if (t.contains(UUID_PLACEHOLDER))
            t = t.replace(UUID_PLACEHOLDER, getMojangUUID(username));
        return t;
    }

    @Nonnull
    private String getMojangUUID(String username) {
        GameProfile profile = MojangAPILoader.loadGameProfile(MojangAPILoader.getMojangApiRoot(), username);
        if (profile == null) {
            CustomSkinLoader.logger.info("UUID for %s not found.", username);
            return "{ABORT}";
        }
        return UUIDTypeAdapter.fromUUID(profile.getId());
    }
}
