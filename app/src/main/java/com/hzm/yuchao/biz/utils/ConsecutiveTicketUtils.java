package com.hzm.yuchao.biz.utils;

import com.hzm.yuchao.biz.model.TicketDO;

import java.util.*;
import java.util.stream.Collectors;

public class ConsecutiveTicketUtils {

    /**
     * 从票列表中找出同一子区域内的连座票
     * @param ticketDOS 可用票列表
     * @param count 需要的票数
     * @param subArea 可选区域筛选（null表示不限制）
     * @return 连座票列表（数量不足或跨子区域时返回空列表）
     */
    public static List<TicketDO> findConsecutiveTicketsInSameSubArea(List<TicketDO> ticketDOS, int count, String subArea) {
        // 入参校验
        if (ticketDOS == null || ticketDOS.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }

        // 1. 筛选符合条件的票（区域可选，不限制子区域，但后续会强制同一子区域）
        List<TicketDO> filteredTicketDOS = ticketDOS.stream()
                .filter(ticketDO -> subArea == null || subArea.equals(ticketDO.getSubArea()))
                .collect(Collectors.toList());

        if (filteredTicketDOS.size() < count) {
            return Collections.emptyList(); // 可用票总数不足
        }

        // 2. 按【区域→子区域→排数】三级分组（核心：先按子区域隔离）
        Map<String, Map<String, Map<Integer, List<TicketDO>>>> groupedTickets =
            groupTicketsByLocation(filteredTicketDOS);

        // 3. 在每个子区域内查找连座（严格限制在同一子区域）
        for (String currentArea : groupedTickets.keySet()) {
            Map<String, Map<Integer, List<TicketDO>>> subAreaMap = groupedTickets.get(currentArea);
            
            // 遍历每个子区域
            for (String currentSubArea : subAreaMap.keySet()) {
                Map<Integer, List<TicketDO>> rowMap = subAreaMap.get(currentSubArea);
                
                // 遍历子区域内的每一排
                for (List<TicketDO> rowTicketDOS : rowMap.values()) {
                    // 对当前排的座位按座位号排序
                    List<TicketDO> sortedTicketDOS = rowTicketDOS.stream()
                            .sorted(Comparator.comparingInt(TicketDO::getSeatNo))
                            .collect(Collectors.toList());

                    // 查找当前排内连续的count个座位（同一子区域内）
                    List<TicketDO> consecutiveTicketDOS = findConsecutiveInRow(sortedTicketDOS, count);
                    if (!consecutiveTicketDOS.isEmpty()) {
                        return consecutiveTicketDOS;
                    }
                }
            }
        }

        // 4. 未找到符合条件的连座票
        return Collections.emptyList();
    }

    /**
     * 按区域、子区域、排数进行三级分组（确保子区域内的票被聚合）
     */
    private static Map<String, Map<String, Map<Integer, List<TicketDO>>>> groupTicketsByLocation(List<TicketDO> ticketDOS) {
        return ticketDOS.stream()
                .collect(Collectors.groupingBy(
                    TicketDO::getArea,  // 一级：区域
                    Collectors.groupingBy(
                        TicketDO::getSubArea,  // 二级：子区域（核心分组，确保连座不跨此层级）
                        Collectors.groupingBy(TicketDO::getSeatRow)  // 三级：排数
                    )
                ));
    }

    /**
     * 在单排中查找连续的n个座位（同一排内）
     */
    public static List<TicketDO> findConsecutiveInRow(List<TicketDO> sortedTicketDOS, int count) {
        // 滑动窗口法：检查连续的count个座位号是否连续
        for (int i = 0; i <= sortedTicketDOS.size() - count; i++) {
            boolean isConsecutive = true;
            
            // 验证从i开始的count个座位是否连续（座位号差为1）
            for (int j = i; j < i + count - 1; j++) {
                if (sortedTicketDOS.get(j + 1).getSeatNo() - sortedTicketDOS.get(j).getSeatNo() != 1) {
                    isConsecutive = false;
                    break;
                }
            }
            
            if (isConsecutive) {
                return sortedTicketDOS.subList(i, i + count); // 返回连续的子列表
            }
        }
        
        return Collections.emptyList(); // 该排无足够连座
    }

    // 测试方法
    public static void main(String[] args) {
        List<TicketDO> ticketDOS = createTestTickets();

        // 测试1：在A区查找3连座（存在于A1子区域）
        List<TicketDO> result1 = ConsecutiveTicketUtils.findConsecutiveTicketsInSameSubArea(ticketDOS, 3, "A1");
        System.out.println("A区的3连座: " + formatTickets(result1));
        // 预期：[A区-A1-1排-1号, A区-A1-1排-2号, A区-A1-1排-3号]

        // 测试2：在A区查找4连座（A2子区域有4连座）
        List<TicketDO> result2 = ConsecutiveTicketUtils.findConsecutiveTicketsInSameSubArea(ticketDOS, 4, "A1");
        System.out.println("A区的4连座: " + formatTickets(result2));
        // 预期：[A区-A2-2排-10号, A区-A2-2排-11号, A区-A2-2排-12号, A区-A2-2排-13号]

        // 测试3：查找跨子区域的连座（应失败，因为A1和A2是不同子区域）
        List<TicketDO> result3 = ConsecutiveTicketUtils.findConsecutiveTicketsInSameSubArea(ticketDOS, 2, "B2");
        System.out.println("A区的2连座: " + formatTickets(result3));
        // 预期：优先返回A1子区域的1-2号（不会跨到A2）
    }

    // 创建测试数据（包含跨子区域的连续座位和同一子区域的连座）
    private static List<TicketDO> createTestTickets() {
        List<TicketDO> ticketDOS = new ArrayList<>();
        // A区A1子区域1排（有3连座）
        ticketDOS.add(new TicketDO("A区", "A1", 1, 1));
        ticketDOS.add(new TicketDO("A区", "A1", 1, 7));
        ticketDOS.add(new TicketDO("A区", "A1", 1, 3));
        ticketDOS.add(new TicketDO("A区", "A1", 1, 5)); // 孤立座位

        // A区A2子区域2排（有4连座）
        ticketDOS.add(new TicketDO("A区", "A2", 2, 1));
        ticketDOS.add(new TicketDO("A区", "A2", 2, 2));
        ticketDOS.add(new TicketDO("A区", "A2", 2, 3));
        ticketDOS.add(new TicketDO("A区", "A2", 2, 4));

        // B区B1子区域（无连座）
        ticketDOS.add(new TicketDO("B区", "B1", 3, 1));
        ticketDOS.add(new TicketDO("B区", "B1", 3, 3));

        return ticketDOS;
    }

    // 格式化票信息输出
    private static String formatTickets(List<TicketDO> ticketDOS) {
        if (ticketDOS.isEmpty()) {
            return "无符合条件的连座票（需同一子区域内）";
        }
        return ticketDOS.stream()
                .map(t -> String.format("%s-%s-%d排-%d号", 
                    t.getArea(), t.getSubArea(), t.getSeatRow(), t.getSeatNo()))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}

