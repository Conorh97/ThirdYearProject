package com.app.efficiclean.classes;

public class QueueHandlerCreater {

    public static QueueHandler createHandler(String hid) {
        //Create instances of JobQueue and TeamQueue for hotel
        JobQueue jQueue = new JobQueue(hid);
        TeamQueue tQueue = new TeamQueue(hid);

        //Create handler for the queues
        QueueHandler qh = new QueueHandler(hid, jQueue, tQueue);

        //Add observer to queues to be notified upon changes
        jQueue.addObserver(qh);
        tQueue.addObserver(qh);

        return qh;
    }
}
