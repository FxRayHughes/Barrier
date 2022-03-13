package ray.mintcat.barrier.utils

import org.bukkit.Location
import ray.mintcat.barrier.common.BarrierPoly


/**
 * * @projectName launcher2
 * * @title Test
 * * @package org.dxl
 * * @description  判断两个多边形是否相交
 * * @author IT_CREAT
 * * @date  2020 2020/11/5/005 22:06
 * * @version
 * * https://blog.csdn.net/IT_CREATE/article/details/109523934
 * * Ray_Hughes 微调了一下
 */
object PolyUtils {
    /*
    第一步：快速排除，以某一个多边形参照物，沿着此多边形画矩形，用矩形将多边形包起来，
    在判断另一个多边形是否在这个矩形框中存在顶点，没有就说明另一个矩形一定在参照矩形框的外围。
    第二步：利用的是向量叉乘求两线段是否相交，这种求法是求的绝对相交（交叉相交），只要存在多边形任意两线段相交，就一定相交，
    同时还有一种情况，就是平行或者在延长线及线段上，这时候需要排除平行和在延长线上的情况
    第三步：用点射法求某一个顶点是否在某个多边形内部，这里需要同时同时判断多变形1上的所有点都在多边形2的内部或者边线段上，
    或者是反向多边形2的所有点在多边形1的内部或边线上，二者满足其一即可；
    */
    fun isCoincidence(p1: BarrierPoly, p2: BarrierPoly): Boolean {
        return intersectionJudgment(p1, p2)
    }

    /**
     * 多边形相交判断(有一个点相交也认为相交)
     *
     * @param polygon1 多边形1
     * @param polygon2 多边形2
     * @return boolean
     */
    fun intersectionJudgment(polygon1: BarrierPoly, polygon2: BarrierPoly): Boolean {
        // 1、快速判断，如果不想交，则返回不相交
        if (!fastExclude(polygon1, polygon2)) {
            return false
        }
        // 获取多边形线段集合
        val lineSegment1 = getLineSegment(polygon1)
        val lineSegment2 = getLineSegment(polygon2)
        // 存在线段集合任意一方为空，则不想交
        // 2、向量叉乘判断多边形是否存在线段相交，存在相交则返回相交
        if (crossJudgment(lineSegment1, lineSegment2)) {
            return true
        }
        // 3、包含关系判断，分别两次判断，判断polygon1是否在polygon2内部和polygon2是否在polygon1内部，满足其一即可
        val isInclude = includeRelation(polygon1, lineSegment2)
        if (isInclude) {
            return true
        }
        return includeRelation(polygon2, lineSegment1)
    }

    /**
     * 1、快速判断多边形是否相交
     *
     * @param polygon1 多边形1
     * @param polygon2 多边形2
     * @return boolean
     */
    fun fastExclude(polygon1: BarrierPoly, polygon2: BarrierPoly): Boolean {
        // 多边形1
        var polygon1MaxX: Double = polygon1.nodes[0].x
        var polygon1MinX: Double = polygon1.nodes[0].x
        var polygon1MaxY: Double = polygon1.nodes[0].z
        var polygon1MinY: Double = polygon1.nodes[0].z
        for (point in polygon1.nodes) {
            polygon1MaxX = polygon1MaxX.coerceAtLeast(point.x)
            polygon1MinX = polygon1MinX.coerceAtMost(point.x)
            polygon1MaxY = polygon1MaxY.coerceAtLeast(point.z)
            polygon1MinY = polygon1MinY.coerceAtMost(point.z)
        }

        // 多边形2
        var polygon2MaxX: Double = polygon2.nodes[0].x
        var polygon2MinX: Double = polygon2.nodes[0].x
        var polygon2MaxY: Double = polygon2.nodes[0].z
        var polygon2MinY: Double = polygon2.nodes[0].z
        for (point in polygon2.nodes) {
            polygon2MaxX = polygon2MaxX.coerceAtLeast(point.x)
            polygon2MinX = polygon2MinX.coerceAtMost(point.x)
            polygon2MaxY = polygon2MaxY.coerceAtLeast(point.z)
            polygon2MinY = polygon2MinY.coerceAtMost(point.z)
        }

        // 我这里是人为临界点的点-点，点-线也是属于相交，（如过你认为不是，加上等于条件，也就是最大和最小出现任意相等也是不想交）
        // 1、多边形1的最大x比多边形2最小x还小，说明多边形1在多边形2左边，不可能相交
        // 2、多边形1的最小x比多边形2最大x还大，说明多边形1在多边形2右边，不可能相交
        // 3、多边形1的最大y比多边形2最小y还小，说明多边形1在多边形2下边，不可能相交
        // 4、多边形1的最小y比多边形2最大y还小，说明多边形1在多边形2上边，不可能相交
        return (polygon1MaxX >= polygon2MinX
                && polygon1MinX <= polygon2MaxX
                && polygon1MaxY >= polygon2MinY
                && polygon1MinY <= polygon2MaxY)
    }

    /**
     * 获取线段集合
     *
     * @param polygon 多边形
     * @return 线段集合
     */
    fun getLineSegment(polygon: BarrierPoly): List<LineSegment> {
        val lineSegments: MutableList<LineSegment> = ArrayList()
        val points = polygon.nodes
        // 依次链接，形成线段
        for (i in 0 until points.size - 1) {
            val previousElement = points[i]
            val lastElement = points[i + 1]
            lineSegments.add(LineSegment(previousElement, lastElement))
        }
        // 最后一组线段（最后一个点和初始点形成最后一条线段，形成闭环）
        if (lineSegments.size > 0) {
            val previousElement = points[points.size - 1]
            val lastElement = points[0]
            lineSegments.add(LineSegment(previousElement, lastElement))
        }
        return lineSegments
    }

    /**
     * 2、线段集合之间是否存在相交关系
     *
     * @param lineSegments1 线段集合1（其中一个多边形的线段集合）
     * @param lineSegments2 线段集合2（另一个多边形的线段集合）
     * @return boolean
     */
    fun crossJudgment(lineSegments1: List<LineSegment>, lineSegments2: List<LineSegment>): Boolean {
        for (lineSegment1 in lineSegments1) {
            for (lineSegment2 in lineSegments2) {
                // 任意一组线段相交及多边形相交，返回相交
                if (calculationLineSegmentCrossing(lineSegment1, lineSegment2)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 线段是否相交（向量叉乘判断）
     *
     * @param lineSegment1 线段1
     * @param lineSegment2 线段2
     * @return boolean
     */
    fun calculationLineSegmentCrossing(lineSegment1: LineSegment, lineSegment2: LineSegment): Boolean {
        // 如果存在任意一点在对方线段上，则相交
        if (isPointOnline(lineSegment1, lineSegment2)) {
            return true
        }
        // 当不存端点在线段上，则进行交叉相交判断
        // A点
        val aPoint = lineSegment1.prePoint
        // B点
        val bPoint = lineSegment1.lastPoint
        // C点
        val cPoint = lineSegment2.prePoint
        // D点
        val dPoint = lineSegment2.lastPoint
        // AB向量叉乘AC向量
        val bc = crossProduct(aPoint, bPoint, aPoint, cPoint)
        // AB向量叉乘AD向量
        val bd = crossProduct(aPoint, bPoint, aPoint, dPoint)
        // CD向量叉乘CA向量
        val da = crossProduct(cPoint, dPoint, cPoint, aPoint)
        // CD向量叉乘CB向量
        val db = crossProduct(cPoint, dPoint, cPoint, bPoint)
        return bc * bd < 0 && da * db < 0
    }

    /**
     * 两线段是否存在点在对方线段上
     *
     * @param lineSegment1 线段1
     * @param lineSegment2 线段2
     * @return boolean
     */
    fun isPointOnline(lineSegment1: LineSegment, lineSegment2: LineSegment): Boolean {
        return isExistTrue(
            booleanArrayOf(
                isCollinearIntersection(lineSegment1.prePoint, lineSegment2),
                isCollinearIntersection(lineSegment1.lastPoint, lineSegment2),
                isCollinearIntersection(lineSegment2.prePoint, lineSegment1),
                isCollinearIntersection(lineSegment2.lastPoint, lineSegment1)
            )
        )
    }

    /**
     * 点是否在线段上
     *
     * @param point            点
     * @param lineSegmentStart 线段起始点
     * @param lineSegmentEnd   线段尾点
     * @return boolean
     */
    fun isCollinearIntersection(
        point: Location,
        lineSegmentStart: Location,
        lineSegmentEnd: Location
    ): Boolean {
        // 排除在延长线上的情况
        return if (point.x >= lineSegmentStart.x.coerceAtMost(lineSegmentEnd.x) && point.x <= lineSegmentStart.x.coerceAtLeast(
                lineSegmentEnd.x
            ) && point.z >= lineSegmentStart.z.coerceAtMost(lineSegmentEnd.z) && point.z <= lineSegmentStart.z.coerceAtLeast(
                lineSegmentEnd.z
            )
        ) {
            // 任意两点之间形成的向量叉乘等于0，表示在线段上或延长线上（三点共线）
            crossProduct(point, lineSegmentStart, point, lineSegmentEnd) == 0.0
        } else false
    }

    /**
     * 点是否在线段上
     *
     * @param point       点
     * @param lineSegment 线段
     * @return boolean
     */
    fun isCollinearIntersection(point: Location, lineSegment: LineSegment): Boolean {
        return isCollinearIntersection(point, lineSegment.prePoint, lineSegment.lastPoint)
    }

    /**
     * 3、多边形polygon的所有点是都在另一个多边形内部（包含关系）
     *
     * @param polygon      被包含（内部）多边形
     * @param lineSegments 包含（外部）多边形所有线段集合
     * @return boolean
     */
    fun includeRelation(polygon: BarrierPoly, lineSegments: List<LineSegment>): Boolean {
        val points = polygon.nodes
        // 所有点在内部或者线段上才算包含，返回包含关系，只要一个不满足，则返回不包含关系
        for (point in points) {
            if (!pointInPolygon(point, lineSegments)) {
                return false
            }
        }
        return true
    }

    /**
     * 判断某个点是否在多边形内部
     *
     * @param point        点
     * @param lineSegments 多边形线段集合
     * @return boolean
     */
    fun pointInPolygon(point: Location, lineSegments: List<LineSegment>): Boolean {
        // 点坐标
        val x: Double = point.x
        val y: Double = point.z
        // 交点个数
        var intersectionNum = 0
        // 判断射线与多边形的交点个数
        for (seg in lineSegments) {
            // 如果点在多边形边线上，则算在多边形内部
            if (isCollinearIntersection(point, seg.prePoint, seg.lastPoint)) {
                return true
            }
            val maxY: Double = seg.prePoint.z.coerceAtLeast(seg.lastPoint.z)
            val minY: Double = seg.prePoint.z.coerceAtMost(seg.lastPoint.z)
            if (y >= minY && y < maxY) {
                // 计算交点X坐标
                    val intersectionPointX =
                    (y - seg.prePoint.z) * (seg.lastPoint.x - seg.prePoint.x) / seg.lastPoint.z - seg.prePoint.z + seg.prePoint.x
                if (x > intersectionPointX) {
                    intersectionNum++
                }
            }
        }
        return intersectionNum % 2 != 0
    }

    /**
     * 向量叉乘
     *
     * @param point1Start 向量1起点
     * @param point1End   向量1尾点
     * @param point2Start 向量2起点
     * @param point2End   向量2尾点
     * @return 向量叉乘结果
     */
    fun crossProduct(
        point1Start: Location,
        point1End: Location,
        point2Start: Location,
        point2End: Location
    ): Double {
        // 向量a
        val aVectorX: Double = point1End.x - point1Start.x
        val aVectorY: Double = point1End.z - point1Start.z
        // 向量b
        val bVectorX: Double = point2End.x - point2Start.x
        val bVectorY: Double = point2End.z - point2Start.z
        // 向量a叉乘向量b
        return aVectorX * bVectorY - bVectorX * aVectorY
    }


    /**
     * 是否存在true
     *
     * @param booleans 布尔集合
     * @return boolean
     */
    fun isExistTrue(booleans: BooleanArray): Boolean {
        for (bool in booleans) {
            if (bool) {
                return true
            }
        }
        return false
    }

    /**
     * 线段
     */
    class LineSegment(
        var prePoint: Location,
        var lastPoint: Location
    )

}