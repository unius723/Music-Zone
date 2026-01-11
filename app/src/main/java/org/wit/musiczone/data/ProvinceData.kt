package org.wit.musiczone.data

data class ProvincePoint(
    val xPercent: Float,
    val yPercent: Float
)
val mapWidth = 1245f
val mapHeight = 686f

val provincePointMap = mapOf(
    "黑龙江省" to ProvincePoint( 1015 / mapWidth, 128 / mapHeight),
    "吉林省" to ProvincePoint(990f / mapWidth, 186f / mapHeight),
    "内蒙古自治区" to ProvincePoint(701f / mapWidth, 238f / mapHeight),
    "辽宁省" to ProvincePoint(947f / mapWidth, 224f / mapHeight),
    "河北省" to ProvincePoint(810f / mapWidth, 268f / mapHeight),
    "北京市" to ProvincePoint(822f / mapWidth, 242f / mapHeight),
    "天津市" to ProvincePoint(836f / mapWidth, 255f / mapHeight),
    "山西省" to ProvincePoint(760f / mapWidth, 296f / mapHeight),
    "陕西省" to ProvincePoint(690f / mapWidth, 358f / mapHeight),
    "宁夏回族自治区" to ProvincePoint(650f / mapWidth, 311f / mapHeight),
    "甘肃省" to ProvincePoint(620f / mapWidth, 344f / mapHeight),
    "青海省" to ProvincePoint(440f / mapWidth, 322f / mapHeight),
    "新疆维吾尔自治区" to ProvincePoint(232f / mapWidth, 227f / mapHeight),
    "西藏自治区" to ProvincePoint(350f / mapWidth, 388f / mapHeight),
    "四川省" to ProvincePoint(575f / mapWidth, 411f / mapHeight),
    "云南省" to ProvincePoint(550f / mapWidth, 510f / mapHeight),
    "重庆市" to ProvincePoint(680f / mapWidth, 423f / mapHeight),
    "贵州省" to ProvincePoint(660f / mapWidth, 488f / mapHeight),
    "广西壮族自治区" to ProvincePoint(687f / mapWidth, 530f / mapHeight),
    "广东省" to ProvincePoint(787f / mapWidth, 522f / mapHeight),
    "海南省" to ProvincePoint(705f / mapWidth, 603f / mapHeight),
    "澳门特别行政区" to ProvincePoint(796f / mapWidth, 549f / mapHeight),
    "香港特别行政区" to ProvincePoint(809f / mapWidth, 550f / mapHeight),
    "台湾省" to ProvincePoint(963f / mapWidth, 512f / mapHeight),
    "湖南省" to ProvincePoint(746f / mapWidth, 460f / mapHeight),
    "江西省" to ProvincePoint(825f / mapWidth, 456f / mapHeight),
    "福建省" to ProvincePoint(890f / mapWidth, 474f / mapHeight),
    "安徽省" to ProvincePoint(853f / mapWidth, 393f / mapHeight),
    "河南省" to ProvincePoint(773f / mapWidth, 356f / mapHeight),
    "江苏省" to ProvincePoint(912f / mapWidth, 363f / mapHeight),
    "山东省" to ProvincePoint(854f / mapWidth, 315f / mapHeight),
    "湖北省" to ProvincePoint(755f / mapWidth, 410f / mapHeight),
    "浙江省" to ProvincePoint(928f / mapWidth, 417f / mapHeight),
    "上海市" to ProvincePoint(971f / mapWidth, 389f / mapHeight)
)