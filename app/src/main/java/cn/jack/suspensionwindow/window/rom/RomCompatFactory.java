package cn.jack.suspensionwindow.window.rom;

/**
 * Created by 大灯泡 on 2018/11/7.
 */
public class RomCompatFactory {

    static final Class<? extends IRomCompat>[] sRomCompat = new Class[]{HuaweiRomCompat.class,
            MeizuRomCompat.class,
            QiKu360RomCompat.class,
            XiaoMiRomCompat.class};

    public static IRomCompat getRomCompatImpl() {
        IRomCompat result = null;
        for (Class<? extends IRomCompat> aClass : sRomCompat) {
            result = find(aClass);
            if (result != null) {
                return result;
            }
        }
        result = new DefaultRomCompat();
        return result;
    }

    private static IRomCompat find(Class<? extends IRomCompat> compat) {
        IRomCompat result = null;
        try {
            result = compat.newInstance();
            if (!result.checkRom()) {
                result = null;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return result;
    }
}
