# 说明
这个项目参考了Handler的源码进行编写，是源码的一个简单版本，但是实现的主要功能是有清晰说明的。

# 单线程模型中Message、Handler、MessageQueue、Looper之间的关系
简单的说，Handler获取当前线程中的looper对象，looper用来从存放Message的MessageQueue中取出Message，再有Handler进行Message的分发和处理.

Message Queue(消息队列)：用来存放通过Handler发布的消息，通常附属于某一个创建它的线程，可以通过Looper.myQueue()得到当前线程的消息队列

Handler：可以发布或者处理一个消息或者操作一个Runnable，通过Handler发布消息，消息将只会发送到与它关联的消息队列，然也只能处理该消息队列中的消息

Looper：是Handler和消息队列之间通讯桥梁，程序组件首先通过Handler把消息传递给Looper，Looper把消息放入队列。Looper也把消息队列里的消息广播给所有的

Handler：Handler接受到消息后调用handleMessage进行处理

Message：消息的类型，在Handler类中的handleMessage方法中得到单个的消息进行处理

在单线程模型下，为了线程通信问题，Android设计了一个Message Queue(消息队列)， 线程间可以通过该Message Queue并结合Handler和Looper组件进行信息交换。下面将对它们进行分别介绍：

1. Message

    Message消息，理解为线程间交流的信息，处理数据后台线程需要更新UI，则发送Message内含一些数据给UI线程。

2. Handler

    Handler处理者，是Message的主要处理者，负责Message的发送，Message内容的执行处理。后台线程就是通过传进来的 Handler对象引用来sendMessage(Message)。而使用Handler，需要implement 该类的 handleMessage(Message)方法，它是处理这些Message的操作内容，例如Update UI。通常需要子类化Handler来实现handleMessage方法。

3. Message Queue

    Message Queue消息队列，用来存放通过Handler发布的消息，按照先进先出执行。

    每个message queue都会有一个对应的Handler。Handler会向message queue通过两种方法发送消息：sendMessage或post。这两种消息都会插在message queue队尾并按先进先出执行。但通过这两种方法发送的消息执行的方式略有不同：通过sendMessage发送的是一个message对象,会被 Handler的handleMessage()函数处理；而通过post方法发送的是一个runnable对象，则会自己执行。

4. Looper

    Looper是每条线程里的Message Queue的管家。Android没有Global的Message Queue，而Android会自动替主线程(UI线程)建立Message Queue，但在子线程里并没有建立Message Queue。所以调用Looper.getMainLooper()得到的主线程的Looper不为NULL，但调用Looper.myLooper() 得到当前线程的Looper就有可能为NULL。对于子线程使用Looper，API Doc提供了正确的使用方法：这个Message机制的大概流程：

    1. 在Looper.loop()方法运行开始后，循环地按照接收顺序取出Message Queue里面的非NULL的Message。

    2. 一开始Message Queue里面的Message都是NULL的。当Handler.sendMessage(Message)到Message Queue，该函数里面设置了那个Message对象的target属性是当前的Handler对象。随后Looper取出了那个Message，则调用 该Message的target指向的Hander的dispatchMessage函数对Message进行处理。在dispatchMessage方法里，如何处理Message则由用户指定，三个判断，优先级从高到低：

        1) Message里面的Callback，一个实现了Runnable接口的对象，其中run函数做处理工作；

        2) Handler里面的mCallback指向的一个实现了Callback接口的对象，由其handleMessage进行处理；

        3) 处理消息Handler对象对应的类继承并实现了其中handleMessage函数，通过这个实现的handleMessage函数处理消息。

    由此可见，我们实现的handleMessage方法是优先级最低的！

    3. Handler处理完该Message (update UI) 后，Looper则设置该Message为NULL，以便回收！

    在网上有很多文章讲述主线程和其他子线程如何交互，传送信息，最终谁来执行处理信息之类的，个人理解是最简单的方法——判断Handler对象里面的Looper对象是属于哪条线程的，则由该线程来执行！

    1. 当Handler对象的构造函数的参数为空，则为当前所在线程的Looper；

    2. Looper.getMainLooper()得到的是主线程的Looper对象，Looper.myLooper()得到的是当前线程的Looper对象。


# Handler的原理

### ThreadLocal

ThreadLocal是一个**线程内部的数据存储类** ，实质上是一个泛型类，定义为：public class ThreadLocal<T>。通过它可以在某个指定线程中存储数据，数据存储以后，只有在**指定线程(存储数据的线程)** 中可以获取到它存储的数据，对于其他的线程来说无法获取到它的数据。

通过使用ThreadLocal，能够让同一个数据对象在不同的线程中存在多个副本，而这些副本互不影响。Looper的实现中便使用到了ThreadLocal。通过使用ThreadLocal，每个线程都有自己的Looper，它们是同一个数据对象的不同副本，并且不会相互影响。

ThreadLocal中有一个内部类ThreadLocalMap，ThreadLocal中有一个内部类Entry，Entry中的`Object value `这个value实际上就是每一个线程中的数据副本。ThreadLocalMap中有一个存放Entry的数组：`Entry[] table`。 ThreadLocal类的部分代码如下：

![image.png](http://upload-images.jianshu.io/upload_images/4143664-db1a4ebe6460dbe3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


ThreadLocal的`set` 方法：实际上就是往ThreadLocalMap对象(map)维护的对象数组table中插入数据。

![image.png](http://upload-images.jianshu.io/upload_images/4143664-35ad6771977585e0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


ThreadLocal的`get` 方法，调用了ThreadLocalMap的getEntry()方法：

![image.png](http://upload-images.jianshu.io/upload_images/4143664-5e5d85fe37f8571f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


ThreadLocalMap的`getEntry()` 方法:

![image.png](http://upload-images.jianshu.io/upload_images/4143664-e2d84bb835c90205.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


i的值是由线程的哈希码和（table的长度-1）进行“按位与”运算，所有每个线程得到的i是不一样的，因此最终数据副本在table中的位置也不一样。

### MessageQueue

MessageQueue主要包含两个操作，插入和读取。读取操作的函数是`next()` ，该操作同时也会伴随着删除操作(相当于出队列)，插入操作对应的函数是`enqueueMessage()` ，`enqueueMessage()` 实际上就是单链表的插入操作。`next()` 方法是一个无限循环的方法，如果消息队列中没有消息，那么next()方法会一直阻塞。当有新消息到来时，next()方法会返回这条消息并将其从单链表中移除。

### Looper

Looper在Android的消息机制中扮演着消息循环的角色，它会不停地从MessageQueue中查看是否有新的Message到来，如果有新消息就会立刻处理，否则就一直阻塞在那里。一个线程只能有一个Looper对象，从而也只有一个MessageQueue(在Looper的构造方法初始化)。

Looper中的几个重要的成员变量：

![image.png](http://upload-images.jianshu.io/upload_images/4143664-5c261017b760ee4d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


Looper的构造方法，在构造方法中，创建了一个**MessageQueue** 实例：

![image.png](http://upload-images.jianshu.io/upload_images/4143664-77da9ca8a0784b14.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


当需要为一个线程创建Looper对象时，需要调用Looper的`prepare()` 方法（该方法在一个线程中只能调用一次，否则会抛出异常）：

![image.png](http://upload-images.jianshu.io/upload_images/4143664-ef2fa558dea89459.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


在`loop()` 的消息循环中，实际上是调用了MessageQueue的**next()** 方法。

![image.png](http://upload-images.jianshu.io/upload_images/4143664-1cc5887a85ec0902.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![image.png](http://upload-images.jianshu.io/upload_images/4143664-25d050169cc47b9e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


Looper主要作用：
1、	与当前线程绑定，保证一个线程只会有一个Looper实例，同时一个Looper实例也只有一个MessageQueue。
2、	loop()方法，不断从MessageQueue中去取消息，交给消息的target属性的dispatchMessage去处理。

### Handler

Handler的工作主要是消息的发送和消息接收处理。消息的发送可以通过Handler的`post()` 方法或者`sendMessage()` 方法来实现，消息的处理，需要我们重写handleMessage()函数来进行处理。

Handler的sendMessage()函数：

![image.png](http://upload-images.jianshu.io/upload_images/4143664-96245925d6a7f74a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](http://upload-images.jianshu.io/upload_images/4143664-30c0909c09a9e66b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](http://upload-images.jianshu.io/upload_images/4143664-28afde61566ded9a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


最后调用了MessageQueue的`enqueueMessage()` 函数：

![image.png](http://upload-images.jianshu.io/upload_images/4143664-26b87b7548a39bfd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](http://upload-images.jianshu.io/upload_images/4143664-3b7d2c3b782f8645.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Message 的callback成员变量实际上是一个**Runnable对象** ：

`Runnable callback; `

经常使用的Handler的`post(Runnable r)` 方法，源码是这样的：

```java

    public final boolean post(Runnable r)
    {
       return  sendMessageDelayed(getPostMessage(r), 0);
    }
```

其中，`getPostMessage(r) ` 为：

```java
private static Message getPostMessage(Runnable r) {
        Message m = Message.obtain();
        m.callback = r;
        return m;
    }
```

原来，Handler的`post()`方法实际上是把这个Runnable对象封装到了一个Message中的。

因此，Handler中的事件处理优先级顺序是：

Message.callback(Runnable) -- >  mCallback(Callback接口实现类或Callback匿名内部类)  --->  Handler或其子类的handleMessage()。
