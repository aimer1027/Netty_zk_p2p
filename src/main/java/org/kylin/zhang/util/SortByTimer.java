package org.kylin.zhang.util;

import org.kylin.zhang.beans.ServerInClusterInfo;

import java.util.Comparator;

/**
 * Created by win-7 on 2015/10/7.
 */
   // 此方法专门用来将 list 中的元素按照 时间新旧 顺序进行排序
    public class SortByTimer implements Comparator {
        public int compare(Object o1, Object o2) {
            ServerInClusterInfo s1 = (ServerInClusterInfo) o1;
            ServerInClusterInfo s2 = (ServerInClusterInfo) o2;

            if ( s1.getSecPast() < s2.getSecPast() ) // 越小说明 数据越新
                return -1;
            return 1;
        }
    }


