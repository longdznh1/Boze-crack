<div align="center">
<h1>boze runnable open source edition</h1>

<img src="https://i.postimg.cc/R0JF9xJK/image.png" alt="logo" width="40%" align="center" />

# [ credits ]

</div>

+ GL_DONT_CARE/Sunsets, Darki, auto - making this client
+ nick - abusing jadyen to deobf it for him
+ ceejay - heavy carrying the remap
+ mrbubbles - fixing errors
+ vineflower - not working
+ procyon - cant handle basic lambdas
+ cfr - schizophrenic local var types
+ javac - useless enum switch tables

<div align="center">

# [ gallery of autism ]

</div>
<img src="https://i.postimg.cc/T35NkRXr/image-47.png" alt=""/>

<div align="center">

# [ how to dump ]

</div>

So, I heard you want to dump boze yourself.

#### Step 1: Access the *unsafe* (this isn't safe, be careful).

Boze doesn’t like you touching its private area. But were going to use Unsafe to force it to let us touch it.

```java
Field f = Unsafe.class.getDeclaredField("theUnsafe");
f.setAccessible(true);
UNSAFE = (Unsafe) f.get(null);
```

#### Step 2: **Untying the Knot**

```java
ClassLoader knot = Knot.getLauncher().getTargetClassLoader();
Object resourceProvider_inst = patch(knot.getClass().getDeclaredField("resourceProvider")).get(knot);
Field rsc = patch(resourceProvider_inst.getClass().getDeclaredField("resources"));
```

#### Step 3: **B is the magic letter?**

If the classess are encrypted how will we deobf it

```java
Class<?> encryption = Class.forName("netutil.d");
Function decrypt_stage0 = (Function) patch(encryption.getDeclaredField("b")).get(null);
Method decrypt_stage1 = patch(encryption.getDeclaredMethod("b", byte[].class));
```

oh wait nvm

#### Step 4: **Time to play garbage man**

```java
HashMap<String, byte[]> resources = (HashMap<String, byte[]>) rsc.get(resourceProvider_inst);
JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar));

resources.forEach((str, data) -> {
    byte[] dec = (byte[]) decrypt_stage0.apply((Object) data);
    if (dec != null) {
        data = (byte[]) decrypt_stage1.invoke(null, dec);
        ClassReader cr = new ClassReader(data);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        str = cn.name;
    }
    jos.putNextEntry(new ZipEntry(str));
    jos.write(data);
    jos.closeEntry();
});
```

#### Step 5: **force jadyen to deobf it**

who doesnt love a little bit of forced labor, it brought you this repository of course!

