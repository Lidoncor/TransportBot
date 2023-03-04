package org.bot;

public class GraspParams {
    Integer m;
    Integer t;
    String r;
    Integer sch;

    GraspParams() { };

    GraspParams(Integer m, Integer t, String r, Integer sch) {
        this.m = m;
        this.t = t;
        this.r = r;
        this.sch = sch;
    }

    /*
        m - номер
        t 1 - автобус
        t 2 - троллейбус
        t 3 - трамвай
        t 8 - маршрутка

        r A -
        r B -

        sch 23 - ежедневно
        sch 5  - выходные
        sch 11 - будни

        grasp.php?tv=mr&m=39&t=1&r=B&sch=5&s=0&v=0
    */
}
