package nick;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.impl.launch.knot.Knot;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class BozeDumper implements ClientModInitializer {
    private static Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Throwable _t) {
            _t.printStackTrace(System.err);
        }
    }

    public static <T extends AccessibleObject> T patch(T obj) {
        UNSAFE.putBoolean(obj, 12, true);
        return obj;
    }

    @Override
    @SuppressWarnings("all")
    public void onInitializeClient() {
        Objects.requireNonNull(UNSAFE);
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(15000);

                ClassLoader knot = Knot.getLauncher().getTargetClassLoader();
                Object resourceProvider_inst = patch(knot.getClass().getDeclaredField("resourceProvider")).get(knot);
                Class<?> resourceProvider_klass = resourceProvider_inst.getClass();
                Field rsc = patch(resourceProvider_klass.getDeclaredField("resources"));

                Class<?> encryption = Class.forName("netutil.d");
                Function decrypt_stage0 = (Function) patch(encryption.getDeclaredField("b")).get(null);
                Method decrypt_stage1 = patch(encryption.getDeclaredMethod("b", byte[].class));

                Path output = Paths.get(System.getProperty("user.home"), "Downloads", "boze_dumped");
                File jar = new File(System.getProperty("user.home") + "\\Downloads", "boze_dump.jar");
                Files.createDirectories(output);

                HashMap<String, byte[]> resources = (HashMap<String, byte[]>) rsc.get(resourceProvider_inst);

                try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
                    resources.forEach((str, data) -> {
                        try {
                            byte[] dec = (byte[]) decrypt_stage0.apply((Object) data);

                            if (dec != null) {
                                data = (byte[]) decrypt_stage1.invoke(null, dec);
                                ClassReader cr = new ClassReader(data);
                                ClassNode cn = new ClassNode();
                                cr.accept(cn, 0);
                                str = cn.name;
                            }

                            ZipEntry ze = new ZipEntry(str);
                            jos.putNextEntry(ze);
                            jos.write(data);
                            jos.closeEntry();
                        } catch (Throwable _t) {
                            _t.printStackTrace(System.err);
                        }
                    });
                }
            } catch (Throwable _t) {
                _t.printStackTrace(System.err);
            }
        });
    }
}