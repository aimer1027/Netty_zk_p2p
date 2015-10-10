package org.kylin.zhang.util;

import org.kylin.zhang.beans.ServerInClusterInfo;

import java.util.Comparator;

/**
 * Created by win-7 on 2015/10/7.
 */
   // �˷���ר�������� list �е�Ԫ�ذ��� ʱ���¾� ˳���������
    public class SortByTimer implements Comparator {
        public int compare(Object o1, Object o2) {
            ServerInClusterInfo s1 = (ServerInClusterInfo) o1;
            ServerInClusterInfo s2 = (ServerInClusterInfo) o2;

            if ( s1.getSecPast() < s2.getSecPast() ) // ԽС˵�� ����Խ��
                return -1;
            return 1;
        }
    }


