-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: book_store_database
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_info`
--

DROP TABLE IF EXISTS `admin_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_info` (
  `admin_id` varchar(8) NOT NULL COMMENT '管理员ID（8位字母数字组合）',
  `admin_password` varchar(64) NOT NULL COMMENT '管理员密码（加密存储）',
  `admin_level` tinyint NOT NULL COMMENT '管理员等级（1或2）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_info`
--

LOCK TABLES `admin_info` WRITE;
/*!40000 ALTER TABLE `admin_info` DISABLE KEYS */;
INSERT INTO `admin_info` VALUES ('admin001','$2a$10$6O7.ihHKDR7neGzqjwO.JeHQ60z/t6ULjnuMkD60FTn09O39PDB3e',2,'2025-11-24 15:14:22'),('P77jeA6h','$2a$10$Pfv5rHHnoERJzJboYtfzxeWdytp15VN.NQN4/37TL0AEmqXCdws56',1,'2025-11-24 19:27:32');
/*!40000 ALTER TABLE `admin_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books_info`
--

DROP TABLE IF EXISTS `books_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books_info` (
  `book_id` int NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
  `book_name` varchar(200) NOT NULL COMMENT '书籍名',
  `category` char(1) NOT NULL COMMENT '书本分类（A-Z）',
  `author` varchar(100) NOT NULL COMMENT '作者',
  `book_image` varchar(500) DEFAULT NULL COMMENT '书籍图片路径',
  `description` text COMMENT '书本简介',
  `publisher` varchar(100) NOT NULL COMMENT '出版社名字',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `rating` decimal(3,1) DEFAULT '0.0' COMMENT '评分（0-10）',
  `stock` int DEFAULT '0' COMMENT '库存量',
  `sales` int DEFAULT '0' COMMENT '总销量',
  `monthly_sales` int DEFAULT '0' COMMENT '月销量',
  `status` tinyint DEFAULT '1' COMMENT '货物状态（1-上架，0-下架）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`book_id`),
  KEY `idx_category` (`category`),
  KEY `idx_author` (`author`),
  KEY `idx_book_name` (`book_name`),
  KEY `idx_sales` (`sales` DESC),
  KEY `idx_rating` (`rating` DESC),
  KEY `idx_monthly_sales` (`monthly_sales` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='书籍信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books_info`
--

LOCK TABLES `books_info` WRITE;
/*!40000 ALTER TABLE `books_info` DISABLE KEYS */;
INSERT INTO `books_info` VALUES (1,'活着','I','余华','uploads/images/books/1.png','《活着》是中国当代作家余华创作的长篇小说，首次发表于《收获》1992年第6期。\n《活着》讲述了在大时代背景下，随着内战、三反五反、大跃进、“文化大革命”等社会变革，徐福贵的人生和家庭不断经受着苦难，到了最后所有亲人都先后离他而去，仅剩下年老的他和一头老牛相依为命。小说以普通、平实的故事情节讲述了在急剧变革的时代中福贵的不幸遭遇和坎坷命运，在冷静的笔触中展现了生命的意义和存在的价值，揭示了命运的无奈，与生活的不可捉摸。\n1994年，改编自该小说的同名剧情片《活着》上映，由张艺谋执导，葛优、巩俐等主演 [19]1998年7月，《活着》获得意大利“格林扎纳·卡佛”文学奖。','北京十月文艺出版社',31.00,9.7,9992,1527,92,1,'2025-11-24 15:14:22','2025-12-03 02:33:06'),(2,'三体','I','刘慈欣','uploads/images/books/2.jpg','科幻巨作，探索宇宙文明','重庆出版社',23.00,9.0,141,2349,129,1,'2025-11-24 15:14:22','2025-11-27 09:16:59'),(3,'百年孤独','I','加西亚·马尔克斯','uploads/images/books/3.jpeg','魔幻现实主义经典','南海出版公司',39.50,4.0,75,985,50,1,'2025-11-24 15:14:22','2025-11-27 09:06:00'),(4,'平凡的世界','I','路遥','uploads/images/books/4.png','《平凡的世界》是中国作家路遥创作的一部全景式地表现中国当代城乡社会生活的百万字长篇小说。全书共三部。1986年12月首次出版。\n该书以中国70年代中期到80年代中期十年间为背景，通过复杂的矛盾纠葛，以孙少安和孙少平两兄弟为中心，刻画了当时社会各阶层众多普通人的形象；劳动与爱情、挫折与追求、痛苦与欢乐、日常生活与巨大社会冲突纷繁地交织在一起，深刻地展示了普通人在大时代历史进程中所走过的艰难曲折的道路，弘扬中华优秀传统文化。\n1991年3月，《平凡的世界》获第三届茅盾文学奖。2019年9月23日，该小说入选“新中国70年70部长篇小说典藏”。','北京十月文艺出版社',98.00,7.3,9989,1660,102,1,'2025-11-24 15:14:22','2025-12-03 02:20:18'),(8,'红楼梦','I','曹雪芹','uploads/images/books/8.png','《红楼梦》，中国古典章回体长篇小说，中国古典四大名著之一。其通行本共120回，一般认为前80回是清代作家曹雪芹（存疑）所著，后40回作者为无名氏，整理者为程伟元、高鹗。小说以贾、史、王、薛四大家族的兴衰为背景，以富贵公子贾宝玉为视角，以贾宝玉与林黛玉、薛宝钗的爱情婚姻悲剧为主线，描绘了上层家族中的人生百态，展现了真实的人性与时代悲剧；是一部从各个角度展现人性以及中国古代社会百态的史诗性著作。\n《红楼梦》版本有120回“程本”和80回“脂本”两大系统。程本为程伟元排印的印刷本，脂本为脂砚斋在不同时期抄评的早期手抄本。脂本是程本的底本。\n《红楼梦》是一部具有世界影响力的人情小说、中国封建社会的百科全书、传统文化的集大成者、中国古代小说巅峰之作、中国古典四大名著之首。其作者以“大旨谈情，实录其事”自勉，只按自己的事体情理，按迹循踪，摆脱旧套，新鲜别致，取得了非凡的艺术成就。“真事隐去，假语存焉”的特殊笔法更是令后世读者脑洞大开，揣测之说久而遂多。二十世纪以来，《红楼梦》更以其丰富深刻的思想底蕴和异常出色的艺术成就使学术界产生了以其为研究对象的专门学问——红学。','中国文联出版社',29.00,5.5,9991,1898,75,1,'2025-11-24 15:14:22','2025-12-03 02:27:11'),(10,'马克思主义基本原理','A','马克思','uploads/images/books/10.jpg','马克思主义基本原理是马克思主义理论体系中最核心的组成部分，集中体现其立场、观点和方法。它以世界观方法论为基础，强调辩证唯物主义与历史唯物主义的统一，在分析社会形态时具有整体性特征 。\n该原理由马克思主义哲学、政治经济学和科学社会主义三大领域构成，包含劳动价值论、剩余价值理论等基本论断。其内容随实践发展动态调整，与中华优秀传统文化在理想追求、价值体系层面存在高度契合性。\n马克思主义基本原理形成于19世纪中叶，马克思恩格斯通过《共产党宣言》《资本论》等著作确立理论框架。列宁主义将其与俄国实际结合推动理论发展，中国特色社会主义理论体系在改革开放中实现马克思主义中国化时代化的新飞跃。','高等教育出版社',23.00,7.0,996,3,3,1,'2025-11-26 13:11:56','2025-11-27 08:43:48'),(11,'简爱','I','夏洛蒂·勃朗特','uploads/images/books/11.png','《简·爱》（Jane Eyre）是英国女作家夏洛蒂·勃朗特创作的长篇小说，是一部具有自传色彩的作品，1847年首次出版。\n该小说讲述孤女简·爱自幼父母双亡，寄养于舅母家，备受虐待，后被舅母打发到洛伍德义塾（洛伍德学校）去。]洛伍德义塾环境恶劣， 但她顽强地活了下来。毕业两年后，简应聘去当家庭教师谋生。主人罗切斯特性格忧郁、喜怒无常，但经过较长时间接触，简发现罗切斯特心地善良，为人正直、刚毅，渐渐对他产生了感情。当他们在教堂举行婚礼时，简痛苦地发现，原来罗切斯特有一个疯妻伯莎·安托瓦妮特·梅森。简悲伤地离去。后来，与她离散多年的叔父约翰·爱病故，遗赠给她巨额财产。因不能忘情于罗切斯特，她重回故地，才知数月前疯女人纵火而死，罗切斯特为救她一只眼睛被砸了出来，另一只眼睛发炎也看不见了。简立刻去向他倾诉衷情，两人终缔良缘。后来，罗切斯特在伦敦医好了一只眼睛，和简·爱生下了一个男孩。\n《简·爱》中简·爱的人生追求有两个基本旋律：富有激情、幻想、反抗和坚持不懈的精神；对人间自由幸福的渴望和对更高精神境界的追求。这本小说的主题是通过孤女坎坷不平的人生经历，成功地塑造了一个不安于现状、不甘受辱、敢于抗争的女性形象，反映一个平凡心灵的坦诚倾诉的呼号和责难，一个小写的人成为一个大写的人的渴望。 该小说被多次改编成舞剧、舞台剧、电视剧和电影。','人民文学出版社',18.00,6.7,9988,1901,78,1,'2025-11-26 13:25:30','2025-12-03 02:20:37'),(12,'C语言程序设计','T','彭慧卿','uploads/images/books/12.png','本书以标准C为框架。按照紧扣基础和面向应用的原则，介绍了C语言程序设计的基本规范、思路和方法。本书从培养学生的实际编程能力出发，注重实例教学和实践练习，突出重点讲解和难点分析，图文并重，文字流畅。本书概念清楚，内容全面，书中所有示例程序均给出了算法思路的分析、算法步骤及运行结果。每个程序都遵循标准化的编程风格，便于学生理解和自学。本书适合作为高等院校各类专业C语言程序设计课程的教材，亦适合初学者自学和供广大程序设计开发人员参考。','清华大学出版社',57.50,5.5,9994,5,5,1,'2025-11-26 13:46:16','2025-11-27 08:50:33'),(39,'共产党宣言','A','马克思','uploads/images/books/39.png','马克思与恩格斯于1848年发表的纲领性文献，首次系统阐述科学社会主义理论，提出“消灭私有制”“全世界无产者联合起来”等核心观点，至今仍是国际共产主义运动的思想旗帜。','人民出版社',45.00,3.0,9997,2,2,1,'2025-12-02 03:11:02','2025-12-03 02:20:18'),(40,'理想国','B','柏拉图','uploads/images/books/40.png','古希腊哲学家柏拉图的经典著作，通过苏格拉底与他人的对话，探讨正义、国家治理、教育等议题，构建了一个由“哲学王”统治的理想城邦模型，奠定西方政治哲学基础。','商务印书馆',68.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:13:19'),(41,'资本论（第一卷）','A','马克思','uploads/images/books/41.png','马克思毕生研究的巅峰之作，揭示资本主义生产方式的运行规律，提出剩余价值理论，剖析资本积累的本质与矛盾，是理解现代经济体系的重要思想资源。','中央编译出版社',98.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:13:31'),(42,'尼各马可伦理学','B','亚里士多德','uploads/images/books/42.png','亚里士多德的伦理学经典，以“幸福”为核心探讨道德德性与实践智慧，主张“中庸之道”，系统阐述了勇气、节制、正义等美德的本质，对西方伦理学发展影响深远。','商务印书馆',55.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:15:54'),(43,'枪炮、病菌与钢铁','C','贾雷德·戴蒙德','uploads/images/books/43.png','贾雷德·戴蒙德的科普巨著，从地理环境视角解释为何欧亚大陆文明最终主导世界，分析粮食生产、动物驯化、病菌传播等因素对人类社会发展的决定性作用，颠覆传统种族优越论。','中信出版社',79.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:16:08'),(44,'社会契约论','D','卢梭','uploads/images/books/44.png','卢梭政治哲学代表作，提出“人生而自由，却无往不在枷锁之中”的命题，主张通过社会契约建立民主政府，强调主权在民与公共意志，为法国大革命提供重要思想武器。','商务印书馆',42.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:16:20'),(45,'经济学原理','F','曼昆','uploads/images/books/45.png','曼昆的经典经济学教材，以“十大经济学原理”为框架，用通俗语言解释供给与需求、市场效率、宏观经济政策等核心概念，兼顾理论深度与现实案例，适合经济学入门读者。','北京大学出版社',65.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:18:37'),(46,'论语译注','H','孔子','uploads/images/books/46.png','杨伯峻先生对《论语》的权威注释本，准确翻译原文并辅以详细注解，完整呈现孔子及其弟子的言行思想，涵盖仁、礼、教育、治国等核心观念，是研读儒家经典的基础读本。','中华书局',32.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:18:47'),(47,'百年孤独','I','马尔克斯','uploads/images/books/47.png','马尔克斯的魔幻现实主义巅峰之作，讲述布恩迪亚家族七代人的传奇故事，融合神话传说、民间故事与现实，用“孤独”主题串联拉丁美洲一个世纪的兴衰史，文字瑰丽奇幻。','南海出版公司',58.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:18:57'),(48,'艺术的故事','J','E.H.贡布里希','uploads/images/books/48.png','贡布里希的艺术史经典，从原始艺术到现代主义，以“没有艺术这回事，只有艺术家”为核心观点，打破传统艺术史的编年史框架，用生动语言解析艺术风格演变的内在逻辑。','广西美术出版社',128.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:19:05'),(49,'全球通史','K','斯塔夫里阿诺斯','uploads/images/books/49.png','斯塔夫里阿诺斯的通史著作，突破西方中心论视角，以全球视野讲述从人类起源到21世纪的文明进程，强调不同文明间的互动与影响，是理解世界历史格局的重要读物。','北京大学出版社',89.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:20:29'),(50,'时间简史','O','霍金','uploads/images/books/50.png','霍金的科普名著，以非专业读者为对象，解释宇宙起源、黑洞、相对论等复杂物理学概念，用通俗比喻和简洁语言探讨时间本质与宇宙命运，兼具科学性与可读性。','湖南科技出版社',55.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:20:37'),(51,'物种起源','Q','达尔文','uploads/images/books/51.png','达尔文划时代的生物学著作，提出“物竞天择，适者生存”的进化论思想，通过大量观察证据论证物种可变与共同起源理论，彻底改变人类对自身与自然的认知。','商务印书馆',68.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:20:55'),(52,'民国语文','G','叶圣陶','uploads/images/books/52.png','收录民国时期中小学语文教材选文，涵盖鲁迅、胡适、朱自清等名家作品及传统经典，展现白话文运动初期的语言风貌与教育理念，对比当代语文教育具有启发意义。','岳麓书社',45.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:21:04'),(53,'现代操作系统','T','Andrew S.Tanenbaum','uploads/images/books/53.png','安德鲁·坦尼鲍姆的计算机教材，系统讲解进程管理、内存管理、文件系统等操作系统核心原理，结合Linux、Windows等实例分析，兼顾理论深度与实践指导，是计算机专业经典教材。','机械工业出版社',99.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:24:15'),(54,'人类简史','C','尤瓦尔·赫拉利','uploads/images/books/54.png','尤瓦尔·赫拉利的全球畅销书，从认知革命、农业革命到科学革命，重新梳理人类发展史，提出“虚构故事”构建人类合作的核心观点，视角宏大且充满颠覆性思考。','中信出版社',69.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:24:24'),(55,'概率论与数理统计','O','陈希孺','uploads/images/books/55.png','高等院校数学教材，系统介绍随机事件、概率分布、假设检验、回归分析等内容，注重理论推导与实际应用结合，例题与习题设计贴近工程、经济等领域需求。','中国科学技术大学出版社',55.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:24:31'),(56,'海权论','U','马汉','uploads/images/books/56.png','马汉的军事战略经典，提出“制海权决定国家兴衰”的核心观点，分析地理、海军力量、海外殖民地等要素对海权的影响，深刻影响近代以来的海军建设与国际战略格局。','商务印书馆',72.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:24:39'),(57,'植物知道生命的答案','Q','丹尼尔·查莫维茨','uploads/images/books/57.png','植物学家丹尼尔·查莫维茨的科普著作，通过实验揭示植物的视觉、嗅觉、触觉等感知能力，证明植物具有复杂的信息处理系统，改变人们对植物“被动存在”的认知。','中信出版社',65.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:24:44'),(58,'社会心理学','B','戴维·迈尔斯','uploads/images/books/58.png','戴维·迈尔斯的经典教材，探讨个体在社会环境中的思维、情感与行为，涵盖从众、偏见、人际关系、群体影响等核心议题，结合大量实验案例，兼具科学性与可读性。','人民邮电出版社',68.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:24:53'),(59,'量子计算与算法导论','O','Peter Shor','uploads/images/books/59.png','介绍量子计算基本原理与核心算法的专业书籍，从量子比特、量子门到Shor算法、Grover算法，兼顾数学基础与编程实现，适合计算机与物理专业读者入门。','清华大学出版社',89.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:24:57'),(60,'城市规划原理','T','吴志强',NULL,'吴良镛院士等编著的规划教材，系统阐述城市规划的理论、方法与实践，涵盖空间布局、交通规划、生态保护等内容，结合中国城市发展案例，是规划领域的权威读本。','中国建筑工业出版社',75.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(61,'认知神经科学','B','史蒂芬·平克',NULL,'迈克尔·加扎尼加主编的神经科学教材，从分子、细胞到脑区网络，解析感知、记忆、语言等认知功能的神经机制，整合神经影像学与心理学研究成果，代表领域前沿水平。','浙江教育出版社',92.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(62,'机器学习实战','T','周志华',NULL,'基于Python语言的机器学习入门书籍，通过实际项目案例讲解分类、回归、聚类等算法实现，涵盖Scikit-learn、TensorFlow等工具使用，注重动手能力培养。','机械工业出版社',99.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(63,'环境经济学','X','潘家华',NULL,'分析环境问题与经济政策关系的学科著作，探讨外部性、公共资源、碳排放交易等核心议题，结合全球环境治理案例，为可持续发展提供经济学视角。','商务印书馆',65.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(64,'现代汉语词典','H','中国社科院语言所',NULL,'中国社会科学院语言研究所编的权威语文工具书，收录现代汉语常用字词，提供准确释义、注音与用法示例，是学习汉语词汇、规范语言使用的必备参考书。','商务印书馆',78.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(65,'中国建筑史','T','刘敦桢',NULL,'梁思成先生的经典著作，系统梳理从先秦到近代的中国建筑发展脉络，分析宫殿、园林、民居等建筑类型的特征与演变，图文并茂，兼具学术性与艺术性。','中国建筑工业出版社',128.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(66,'神经科学原理','B','埃里克·坎德尔',NULL,'坎德尔主编的神经科学百科全书式教材，涵盖神经细胞生物学、突触传递、感觉系统等内容，整合分子生物学与系统神经科学视角，是领域内的权威参考资料。','北京大学出版社',115.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(67,'算法导论','T','Thomas H.Cormen',NULL,'计算机算法领域的经典教材，深入讲解排序、图论、动态规划等算法设计与分析方法，注重数学证明与复杂度分析，适合计算机专业高年级学生及研究者。','机械工业出版社',129.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(68,'世界现代设计史','J','王受之',NULL,'王受之教授的设计史著作，从工业革命到后现代主义，解析平面设计、产品设计、建筑设计等领域的风格演变，探讨设计与社会、技术、艺术的互动关系。','中国青年出版社',86.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(69,'细胞生物学','Q','翟中和',NULL,'翟中和院士主编的高校教材，系统阐述细胞的结构与功能，涵盖细胞膜、细胞器、细胞信号转导、细胞周期等内容，结合分子生物学研究进展，体现学科前沿。','高等教育出版社',72.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(70,'金融学','F','博迪',NULL,'黄达教授的金融学教材，涵盖货币银行学、国际金融、金融市场等核心内容，兼顾理论体系与中国金融实践，注重宏观金融与微观金融的结合，适合经济类专业学生。','中国人民大学出版社',98.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(71,'中国哲学简史','B','冯友兰',NULL,'冯友兰先生的哲学入门书，以西方哲学框架梳理中国哲学发展脉络，从先秦诸子到近代新儒家，简明扼要呈现儒、道、墨、法等主要学派的核心思想。','北京大学出版社',56.00,0.0,999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(72,'信息检索与利用','G','王立名',NULL,'介绍信息检索原理与实践的应用书籍，涵盖搜索引擎技术、数据库检索、文献管理等内容，结合数字时代信息素养培养需求，适合大学生及科研人员使用。','清华大学出版社',45.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(73,'航空航天材料','V','赵振业',NULL,'探讨航空航天领域材料科学与工程的专业书籍，分析金属材料、复合材料、功能材料的性能要求与应用场景，结合飞行器设计案例，体现材料技术对航空航天发展的支撑作用。','国防工业出版社',88.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(74,'管理学','F','斯蒂芬·罗宾斯',NULL,'罗宾斯的经典管理学教材，系统讲解计划、组织、领导、控制等管理职能，涵盖激励理论、组织文化、战略管理等内容，结合全球企业案例，适合管理学科入门。','中国人民大学出版社',79.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(75,'中国通史','K','白寿彝',NULL,'白寿彝总主编的多卷本通史著作，从远古到近代，全面阐述中国政治、经济、文化、社会的发展历程，兼顾学术性与可读性，是中国通史研究的重要成果。','上海人民出版社',158.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(76,'人工智能导论','T','李开复',NULL,'介绍人工智能基本原理与技术的入门书籍，涵盖机器学习、自然语言处理、计算机视觉等领域，结合实际应用案例，适合计算机、自动化等专业学生及爱好者。','人民邮电出版社',69.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(77,'现代物流管理','F','唐纳德·沃兹尔',NULL,'探讨现代物流系统规划与运营的专业书籍，涵盖仓储管理、运输优化、供应链协同等内容，结合电商物流、智慧物流等新业态，体现物流管理的前沿发展。','机械工业出版社',65.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02'),(78,'西方美术史','J','范景中',NULL,'梳理西方美术从古希腊罗马到现代主义的发展历程，分析文艺复兴、巴洛克、印象派等重要艺术流派的代表艺术家与作品，探讨艺术风格与社会文化的关联。','中国美术学院出版社',95.00,0.0,9999,0,0,1,'2025-12-02 03:11:02','2025-12-02 03:11:02');
/*!40000 ALTER TABLE `books_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carousel_items`
--

DROP TABLE IF EXISTS `carousel_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carousel_items` (
  `carousel_id` int NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `book_id` int NOT NULL COMMENT '书籍ID',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`carousel_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `carousel_items_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books_info` (`book_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carousel_items`
--

LOCK TABLES `carousel_items` WRITE;
/*!40000 ALTER TABLE `carousel_items` DISABLE KEYS */;
INSERT INTO `carousel_items` VALUES (1,1,0,'2025-11-26 12:57:35'),(2,2,2,'2025-11-26 12:58:52'),(3,3,3,'2025-11-26 13:01:04');
/*!40000 ALTER TABLE `carousel_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `picture`
--

DROP TABLE IF EXISTS `picture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `picture` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `picture`
--

LOCK TABLES `picture` WRITE;
/*!40000 ALTER TABLE `picture` DISABLE KEYS */;
INSERT INTO `picture` VALUES (1,'jjjj','../picture\\jjjj.jpg','2025-11-26 04:27:51');
/*!40000 ALTER TABLE `picture` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `cart_id` int NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `book_id` int NOT NULL COMMENT '书籍ID',
  `quantity` int DEFAULT '1' COMMENT '数量',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `unique_user_book` (`user_id`,`book_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `shopping_cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `shopping_cart_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books_info` (`book_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_hist`
--

DROP TABLE IF EXISTS `shopping_hist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_hist` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `book_id` int NOT NULL COMMENT '书籍ID',
  `quantity` int NOT NULL COMMENT '购买数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价',
  `order_status` tinyint DEFAULT '1' COMMENT '订单状态（1-已完成，0-已取消）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  PRIMARY KEY (`order_id`),
  KEY `book_id` (`book_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at` DESC),
  CONSTRAINT `shopping_hist_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `shopping_hist_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books_info` (`book_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='历史订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_hist`
--

LOCK TABLES `shopping_hist` WRITE;
/*!40000 ALTER TABLE `shopping_hist` DISABLE KEYS */;
INSERT INTO `shopping_hist` VALUES (1,100002,11,1,18.00,18.00,1,'2025-11-26 14:36:26'),(2,100002,4,1,98.00,98.00,1,'2025-11-26 14:36:58'),(3,100002,8,3,29.00,87.00,1,'2025-11-26 14:36:58'),(4,100002,4,1,98.00,98.00,1,'2025-11-27 08:26:09'),(5,100002,12,4,57.50,230.00,1,'2025-11-27 08:26:26'),(6,100002,2,9,23.00,207.00,1,'2025-11-27 08:31:00'),(7,100002,1,2,31.00,62.00,1,'2025-11-27 08:31:22'),(8,100023,11,7,18.00,126.00,1,'2025-11-27 08:43:36'),(9,100023,10,3,23.00,69.00,1,'2025-11-27 08:43:48'),(10,100023,3,3,39.50,118.50,1,'2025-11-27 08:44:01'),(11,100023,1,4,31.00,124.00,1,'2025-11-27 08:44:21'),(12,100024,4,3,98.00,294.00,1,'2025-11-27 08:50:17'),(13,100024,12,1,57.50,57.50,1,'2025-11-27 08:50:33'),(14,100025,11,2,18.00,36.00,1,'2025-11-27 08:57:12'),(15,100026,4,1,98.00,98.00,1,'2025-11-27 08:58:44'),(16,100028,8,3,29.00,87.00,1,'2025-11-27 09:05:53'),(17,100028,3,2,39.50,79.00,1,'2025-11-27 09:06:00'),(18,100028,1,1,31.00,31.00,1,'2025-11-27 09:06:20'),(19,100002,4,4,98.00,392.00,1,'2025-12-03 02:20:18'),(20,100002,39,2,45.00,90.00,1,'2025-12-03 02:20:18'),(21,100002,11,1,18.00,18.00,1,'2025-12-03 02:20:37'),(22,100002,8,1,29.00,29.00,1,'2025-12-03 02:22:08'),(23,100002,8,1,29.00,29.00,1,'2025-12-03 02:27:11');
/*!40000 ALTER TABLE `shopping_hist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_add`
--

DROP TABLE IF EXISTS `user_add`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_add` (
  `address_id` int NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `address` varchar(500) NOT NULL COMMENT '地址',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址（1-是，0-否）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`address_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `user_add_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_add`
--

LOCK TABLES `user_add` WRITE;
/*!40000 ALTER TABLE `user_add` DISABLE KEYS */;
INSERT INTO `user_add` VALUES (2,100002,'我家',0,'2025-11-26 13:18:02'),(4,100002,'上海理工大学春江路外卖柜',1,'2025-11-26 13:18:20'),(5,100003,'测试地址1号',1,'2025-11-27 08:36:45'),(6,100004,'测试地址2号',1,'2025-11-27 08:36:45'),(7,100005,'测试地址3号',1,'2025-11-27 08:36:45'),(8,100006,'测试地址4号',1,'2025-11-27 08:36:45'),(9,100007,'测试地址5号',1,'2025-11-27 08:36:45'),(10,100008,'测试地址6号',1,'2025-11-27 08:36:45'),(11,100009,'测试地址7号',1,'2025-11-27 08:36:45'),(12,100010,'测试地址8号',1,'2025-11-27 08:36:45'),(13,100011,'测试地址9号',1,'2025-11-27 08:36:45'),(14,100012,'测试地址10号',1,'2025-11-27 08:36:45'),(15,100013,'测试地址11号',1,'2025-11-27 08:36:45'),(16,100014,'测试地址12号',1,'2025-11-27 08:36:45'),(17,100015,'测试地址13号',1,'2025-11-27 08:36:45'),(18,100016,'测试地址14号',1,'2025-11-27 08:36:45'),(19,100017,'测试地址15号',1,'2025-11-27 08:36:45'),(20,100018,'测试地址16号',1,'2025-11-27 08:36:45'),(21,100019,'测试地址17号',1,'2025-11-27 08:36:45'),(22,100020,'测试地址18号',1,'2025-11-27 08:36:45'),(23,100021,'测试地址19号',1,'2025-11-27 08:36:45'),(24,100022,'测试地址20号',1,'2025-11-27 08:36:45'),(25,100023,'测试地址21号',1,'2025-11-27 08:45:31'),(26,100028,'测试地址25号',1,'2025-11-27 09:05:38');
/*!40000 ALTER TABLE `user_add` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID，从100000开始',
  `username` varchar(48) NOT NULL COMMENT '用户名，最多16个中文字符',
  `phone` varchar(11) NOT NULL COMMENT '用户电话号码',
  `password` varchar(64) NOT NULL COMMENT '用户密码（加密存储）',
  `user_level` tinyint DEFAULT '0' COMMENT '用户等级（0-5级）',
  `total_spending` decimal(10,2) DEFAULT '0.00' COMMENT '累计消费金额',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=100030 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_info`
--

LOCK TABLES `user_info` WRITE;
/*!40000 ALTER TABLE `user_info` DISABLE KEYS */;
INSERT INTO `user_info` VALUES (100002,'MOULONG','18930440598','$2a$10$ayZlZ5Ej2UWaSt4U/2ejLuXrRZ4bs2H.9Seb6L5JSmZ/k797xrmHi',4,1358.00,'2025-11-26 12:34:53','2025-12-03 02:27:11'),(100003,'test1','13900000001','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100004,'test2','13900000002','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100005,'test3','13900000003','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100006,'test4','13900000004','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100007,'test5','13900000005','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100008,'test6','13900000006','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100009,'test7','13900000007','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100010,'test8','13900000008','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100011,'test9','13900000009','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100012,'test10','13900000010','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100013,'test11','13900000011','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100014,'test12','13900000012','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100015,'test13','13900000013','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100016,'test14','13900000014','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100017,'test15','13900000015','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100018,'test16','13900000016','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100019,'test17','13900000017','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100020,'test18','13900000018','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100021,'test19','13900000019','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100022,'test20','13900000020','$2a$10$N9qo8uLOickgx2ZMRZoMye9YrjHkpXKtqM1hvdJzDXHFVOVYUm3ay',0,0.00,'2025-11-27 08:36:45','2025-11-27 08:36:45'),(100023,'test21','13900000021','$2a$10$dFvT0.LaeEs.0ngYyOo8f.kVCnmOhcpakpNE0g8XT.GGztzH5kqZi',3,437.50,'2025-11-27 08:38:51','2025-11-27 08:44:21'),(100024,'test22','13900000022','$2a$10$yU2/q8SNOwTSyX9WbN4KZe4NGBj9BgqPz737WbDU7Xa46KKidgfze',3,351.50,'2025-11-27 08:46:48','2025-11-27 08:50:33'),(100025,'test23','13900000023','$2a$10$xUyyL9nMc7ewxCXEdWWFA.gwN.WzudbrphpUdrZoJ.uwPK2QA2hnm',1,36.00,'2025-11-27 08:50:59','2025-11-27 08:57:12'),(100026,'test24','13900000024','$2a$10$cuvhZEWSLPRvR6OiTzOcoeej1SiFVE7f1yJqSrIkQyD0Q28hOscJK',1,98.00,'2025-11-27 08:57:34','2025-11-27 08:58:44'),(100028,'test25','13900000025','$2a$10$U9XS9RYdEmdZ.vJgw5dAVetN3pqsnLX4VCcSTo7Up9IvRSOHjgvDq',1,197.00,'2025-11-27 09:04:39','2025-11-27 09:06:20'),(100029,'test26','13900000026','$2a$10$RsWvIbBQLQCEBINmIF4XVuCPi5JoKXk5Ve0siLw9O4hA0X2ZOuiGu',0,0.00,'2025-12-02 01:19:15','2025-12-02 01:19:15');
/*!40000 ALTER TABLE `user_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_score`
--

DROP TABLE IF EXISTS `user_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_score` (
  `user_id` int NOT NULL COMMENT '用户ID',
  `book_id` int NOT NULL COMMENT '书籍ID',
  `score` tinyint NOT NULL COMMENT '评分（1-10）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  PRIMARY KEY (`user_id`,`book_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `user_score_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `user_score_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books_info` (`book_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户评分表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_score`
--

LOCK TABLES `user_score` WRITE;
/*!40000 ALTER TABLE `user_score` DISABLE KEYS */;
INSERT INTO `user_score` VALUES (100002,1,10,'2025-11-27 08:31:21'),(100002,2,9,'2025-11-27 08:30:58'),(100002,4,6,'2025-11-26 14:40:27'),(100002,8,4,'2025-11-27 08:44:57'),(100002,11,8,'2025-11-26 14:40:18'),(100002,12,2,'2025-11-27 08:26:21'),(100002,39,3,'2025-12-03 02:20:03'),(100023,1,9,'2025-11-27 08:44:17'),(100023,3,5,'2025-11-27 08:43:53'),(100023,10,7,'2025-11-27 08:43:45'),(100023,11,4,'2025-11-27 08:43:33'),(100024,4,7,'2025-11-27 08:50:15'),(100024,12,9,'2025-11-27 08:50:26'),(100025,11,8,'2025-11-27 08:57:10'),(100026,4,9,'2025-11-27 08:58:43'),(100028,1,10,'2025-11-27 09:06:19'),(100028,3,3,'2025-11-27 09:05:59'),(100028,8,7,'2025-11-27 09:05:52');
/*!40000 ALTER TABLE `user_score` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-03 10:47:31
