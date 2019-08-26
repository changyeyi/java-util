package org.changyeyi.util;

import org.changyeyi.model.Point;

import java.util.Random;

/**
 * 经纬处理工具类
 * 随机点位计算方法，实现参考http://www.geomidpoint.com/random/calculation.html
 * 一些其他的经纬度计算实现，http://www.movable-type.co.uk/scripts/latlong.html
 * @author changyeyi
 * @version 1.0.0
 * @date 2019-08-24
 */
public class LngLatUtil {

    private static double EARTH_RADIUS_KM = 6372.796924;
    private static double EARTH_RADIUS_MILES = 3960.056052;
    private static double PI = 3.1415926535898;

    /**
     * 计算两点间的距离，返回米
     * haversine，球面三角函数
     * 公式：a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
     * formula  公式：c = 2 ⋅ atan2( √a, √(1−a) )
     *                d= R ⋅ c
     */
    public static double getDistance(double startLat, double startLng, double endLat, double endLng){
        // haversine
        double startRadLat = rad(startLat);
        double endRadLat = rad(endLat);
        double a = startRadLat - endRadLat;
        double b = rad(startLng) - rad(endLng);
        double haverSine = Math.pow(Math.sin(a / 2), 2) + Math.cos(startRadLat) * Math.cos(endRadLat) * Math.pow(Math.sin(b / 2), 2);
        // formula
        double s = 2 * Math.atan2(Math.sqrt(haverSine),Math.sqrt(1-haverSine));
        // 计算实际长度
        s = s * EARTH_RADIUS_KM;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return Math.round(s);
    }

    /**
     * 圆内生成随机经纬度
     * 实现细节：
     * 1、将所有纬度和经度转换为弧度。
     * 2、rand1和rand2是在0到1.0范围内生成的唯一随机数。
     * 3、获取初始值startLat，startLon和maxDist。（maxDist单位是英里或者公里）
     * 4、对于地球的平均半径使用：
     * radiusEarth = 3960.056052 miles or radiusEarth = 6372.796924 km
     * 5、将最大距离转换为弧度。
     * maxDist=maxDist/radiusEarth
     * 6、计算从0到maxDist缩放的随机距离，使得较大圆上的点比较小圆上的点具有更大的选择概率，如前所述。
     * dist = acos(rand1*(cos(maxDist) - 1) + 1)
     * 7、计算从0到2 * PI弧度（0到360度）的随机轴承，所有轴承具有相同的选择概率。
     * brg = 2*PI*rand2
     * 8、使用起点，随机距离和随机方位来计算最终随机点的坐标
     * lat = asin(sin(startlat)*cos(dist) + cos(startlat)*sin(dist)*cos(brg))
     * lon = startlon + atan2(sin(brg)*sin(dist)*cos(startlat), cos(dist)-sin(startlat)*sin(lat))
     * 9、如果lon < -PI，lon=lon+2*PI
     *    如果lon > PI，lon=lon-2*PI
     *    注：
     *      因为经度是正负180度
     * 10、弧度转换为角度，也就是经纬度
     * 11、保留八位小数
     *
     * @param center   圆心
     * @param distance 半径，单位米
     */
    public static Point getRandomLocation(Point center, double distance) {
        // 1
        double startLat = rad(center.getLatitude());
        double startLon = rad(center.getLongitude());
        // 2
        double rand1 = new Random().nextDouble();
        double rand2 = new Random().nextDouble();
        // 5
        double maxDist = distance / 1000 / EARTH_RADIUS_KM;
        // 6
        double dist = Math.acos(rand1 * (Math.cos(maxDist) - 1) + 1);
        // 7
        double brg = 2 * PI * rand2;
        // 8
        double lat = Math.asin(Math.sin(startLat) * Math.cos(dist) + Math.cos(startLat) * Math.sin(dist) * Math.cos(brg));
        double lon = startLon + Math.atan2(Math.sin(brg) * Math.sin(dist) * Math.cos(startLat), Math.cos(dist) - Math.sin(startLat) * Math.sin(lat));
        // 9
        if (lon < -PI) {
            lon = lon + 2 * PI;
        } else if (lon > PI) {
            lon = lon - 2 * PI;
        }
        // 10
        lon = deg(lon);
        lat = deg(lat);
        System.out.println(lon+","+lat);
        // 11
        return new Point(padZeroRight(lat), padZeroRight(lon));
    }

    /**
     * 矩形内随机生成经纬度
     * 实现细节：
     * 1、将所有纬度和经度转换为弧度。
     * 2、rand1和rand2是在0到1.0范围内生成的唯一随机数。
     * 3、获取初始纬度northlimit and southlimit，以及经度westlimit和eastlimit。
     * 4、计算随机纬度，使得矩形中较长纬度线上的点比较短纬度线上的点更可能被选择。
     *      lat = asin（rand1 *（sin（northlimit） - sin（southlimit））+ sin（southlimit））
     * 5、找到矩形区域的宽度。
     *      width = eastlimit - westlimit
     * 6、如果width小于0，则：
     *      width = width + 2 * PI
     * 7、计算westlimit和eastlimit之间的随机经度，所有经度具有相同的被选择概率。
     *      lon = westimit + width * rand2
     * 8、如果lon小于-PI则：
     *      lon = lon + 2 * PI
     *    如果lon大于PI，则：
     *      lon = lon - 2 * PI
     * 9、弧度转换为角度，也就是经纬度
     * 10、保留八位小数
     */
    public static Point getRandomLocation(double eastLimit, double westLimit, double southLimit, double northLimit) {
        // 1
        northLimit = rad(northLimit);
        southLimit = rad(southLimit);
        westLimit = rad(westLimit);
        eastLimit = rad(eastLimit);
        // 4
        double lat = Math.asin(new Random().nextDouble() * (Math.sin(northLimit) - Math.sin(southLimit)) + Math.sin(southLimit));
        // 5
        double width = eastLimit - westLimit;
        // 6
        if (width < 0) {
            width = width + 2 * PI;
        }
        // 7
        double lon = westLimit + width * new Random().nextDouble();

        // 8
        if (lon < -PI) {
            lon = lon + 2 * PI;
        } else if (lon > PI) {
            lon = lon - 2 * PI;
        }
        lat = deg(lat);
        lon = deg(lon);
        return new Point(padZeroRight(lat), padZeroRight(lon));
    }

    /**
     * 角度，也就是经纬度
     */
    private static double deg(double rd) {
        return (rd * 180 / Math.PI);
    }

    /**
     * 计算弧度，弧长等于半径的弧，弧度为1
     */
    private static double rad(double d) {
        return d * PI / 180.0;
    }

    /**
     * 取八位小数
     */
    private static double padZeroRight(double s) {
        double sigDigits = 8;
        s = Math.round(s * Math.pow(10, sigDigits)) / Math.pow(10, sigDigits);
        return s;
    }

}
