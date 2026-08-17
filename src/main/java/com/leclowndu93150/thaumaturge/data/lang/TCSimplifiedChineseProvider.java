package com.leclowndu93150.thaumaturge.data.lang;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCSimplifiedChineseProvider extends LanguageProvider {
    public TCSimplifiedChineseProvider(PackOutput output) {
        super(output, TCIds.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.thaumaturge", "Thaumaturge");

        addJade();

        aspect("aer", "风", "气，风", "风");
        aspect("terra", "地", "地", "地");
        aspect("ignis", "火", "火", "火");
        aspect("aqua", "水", "水", "水");
        aspect("ordo", "秩序", "秩序，规则，纯净", "秩序");
        aspect("perditio", "混沌", "熵增，混沌，解构", "毁灭之物");

        aspect("vacuos", "虚空", "虚空", "空虚之物");
        aspect("lux", "光", "光", "光明");
        aspect("motus", "移动", "移动，动作", "动起来的玩意儿");
        aspect("gelum", "寒冰", "寒冰，霜冻，寒冷", "冰冷之物");
        aspect("vitreus", "水晶", "水晶，玻璃，澄澈", "水晶");
        aspect("metallum", "金属", "金属", "金属");
        aspect("victus", "生命", "生命", "生命之源");
        aspect("mortuus", "死亡", "死亡", "死亡的天性");
        aspect("potentia", "能量", "能量，动力", "能量");
        aspect("permutatio", "交换", "交换，贸易", "贸易与交换");
        aspect("praecantatio", "魔力", "魔力，咒语，附魔", "魔法之物");
        aspect("auram", "灵气", "灵气", "灵气");
        aspect("alkimia", "炼金", "炼金术，化学", "炼金术");
        aspect("vitium", "污染", "污染，变化，突变", "魔力之腐化影响");
        aspect("tenebrae", "黑暗", "黑暗", "黑暗");
        aspect("alienis", "异域", "异域，怪异，诡谲", "异域奇物");
        aspect("volatus", "飞行", "飞行", "飞行");
        aspect("herba", "植物", "植物", "植物");
        aspect("instrumentum", "工具", "工具，仪器", "工具");
        aspect("fabrico", "合成", "合成", "加工");
        aspect("machina", "机械", "机构，机械", "机械品");
        aspect("vinculum", "陷阱", "陷阱，监禁", "陷入罗网之物");
        aspect("spiritus", "灵魂", "灵魂", "精神");
        aspect("cognitio", "思维", "思维，记忆，认识", "头脑");
        aspect("sensus", "感官", "感官", "感知");
        aspect("aversio", "冲突", "憎恶，冲突", "冲突");
        aspect("praemunio", "守护", "强化，保护，守护", "守护之物");
        aspect("desiderium", "贪婪", "愿望，意愿，渴望，需求", "贵重之物");
        aspect("exanimis", "亡灵", "不死，亡灵", "不死的天性");
        aspect("bestia", "野兽", "野兽", "野兽");
        aspect("humanus", "人类", "人类", "人类");

        researchCategory("basics", "基础学");
        researchCategory("auromancy", "神秘学");
        researchCategory("alchemy", "炼金术");
        researchCategory("artifice", "炼化学");
        researchCategory("infusion", "奥术注魔");
        researchCategory("golemancy", "傀儡学");
        researchCategory("eldritch", "邪术学");

        card("study", "研究：%s");
        card("analyze", "分析：%s");
        card("balance", "平衡");
        card("ponder", "沉思");
        card("inspired", "启发");
        card("notation", "标记");
        card("rethink", "反思");
        card("reject", "拒绝：%s");
        card("experimentation", "实验");

        add("resourcePack.thaumaturge.programmer_art.name", "Thaumaturge Programmer Art");
        add("knowledge_type.thaumaturge.theory", "理论");
        add("knowledge_type.thaumaturge.observation", "观测");
        add("tc.knowledge.tooltip", "%1$s：%2$s");
        add("tc.research_category.percent", "%1$s（%2$s%%）");
        add("tc.need.obtain", "需要的物品");
        add("tc.need.craft", "需要合成的物品");
        add("tc.need.research", "其他需要的研究");
        add("tc.need.know", "需要使用的知识");
        add("tc.research.stage", "当前阶段：");
        add("tc.knowledge.none", "你尚未积累任何知识。用魔导透镜扫描世界，或在研究台上进行研究。");
        add("tc.research.stage.short", "%1$s / %2$s");
        add("tc.research.begin", "你还没开始这项研究");
        add("tc.researchmissing", "缺失前置研究：");
        add("tc.research.newresearch", "§6新发现的研究§0");
        add("tc.research.newpage", "§a已添加新页面§0");
        add("tc.search", "搜索");
        add("tc.search.more", "……结果太多了，请细化搜索内容");
        add("tile.researchtable.noink.0", "墨水已耗尽！");
        add("tile.researchtable.noink.1", "请补充新的笔与墨。");
        add("tile.researchtable.nopaper.0", "纸已耗尽！");
        add("tc.card.unknown", "未知的物品");
        add("tc.warp.warn", "扭曲等级：%n");
        add("tc.forbidden", "§l§5禁忌知识（%n）§0");
        add("tc.forbidden.level.1", "近乎无害");
        add("tc.forbidden.level.2", "略有风险");
        add("tc.forbidden.level.3", "稍有危险");
        add("tc.forbidden.level.4", "危险");
        add("tc.forbidden.level.5", "禁忌");
        add("tc.inst", "风险");
        add("tc.inst.0", "§1微乎其微§0");
        add("tc.inst.1", "§9微量§0");
        add("tc.inst.2", "§5适量§0");
        add("tc.inst.3", "§e高§0");
        add("tc.inst.4", "§6极高§0");
        add("tc.inst.5", "§4危险§0");
        add("tc.type.theory", "理论");
        add("tc.type.observation", "观测");
        add("recipe.return", "返回");
        add("recipe.clickthrough", "点击查看配方");
        add("recipe.unknown", "你暂时不能合成这个");
        add("recipe.type.workbench", "工作台");
        add("recipe.type.workbenchshapeless", "工作台（无序合成）");
        add("recipe.type.smelting", "冶炼");
        add("recipe.type.arcane", "奥术工作台");
        add("recipe.type.arcane.shapeless", "奥术工作台（无序合成）");
        add("recipe.type.crucible", "坩埚");
        add("recipe.type.infusion", "奥术注魔");
        add("recipe.type.infusion_enchantment", "注魔法附魔");
        add("tooltip.thaumaturge.charge", "魔力：%s / %s");
        add("tooltip.thaumaturge.runic_charge", "符文护盾 +%s");
        add("tooltip.thaumaturge.infusion_stabiliser", "注魔稳定器");
        add("recipe.type.runic_augment", "符文强化");
        add("recipe.type.construct", "神秘多方块结构");
        add("wandtable.text1", "魔力消耗");
        add("gui.thaumaturge.research_table.title", "研究台");
        add("gui.thaumaturge.deconstruction_table.title", "解构工作台");
        add("block.thaumaturge.deconstruction_table", "解构工作台");
        add("item.thaumaturge.research_note", "研究笔记");
        add("item.thaumaturge.research_note.complete", "发现！");
        add("tc.researchtheory", "理论：%s");
        add("tc.researchnote.click", "点击获取研究笔记（需要纸张与抄写工具）");
        add("tc.researchnote.table", "在研究台上完成这些笔记");
        add("tc.researchnote.use", "右键以学习此理论");
        add("tc.researchnote.learned", "你完成了关于 %s 的研究！");
        add("tc.researchnote.missing", "你需要抄写工具与纸张才能获取这份研究笔记！");
        add("tc.addaspectdiscovery", "你发现了要素 %s！");
        add("tc.discoveryerror", "要窥破个中真谛你需要研究%1$s。");
        add("tc.aspectcost", "所需研究点数：");
        add("tc.research.copy", "复制这些研究笔记");
        add("tc.decon.collect", "点击以收集此研究点数");
        add("tc.table.combine", "将两种要素合成为其复合要素");
        add("tc.table.helper", "要素合成参考");
        add("tc.device.unknown", "你尚不理解此装置的工作原理。");
        add("tc.table.select", "将要素拖拽至此以合成");
        add("tc.table.page.prev", "上一页");
        add("tc.table.page.next", "下一页");
        add("tc.table.slot.tools", "在此放置抄写工具");
        add("tc.table.slot.note", "在此放置研究笔记");
        add("research.thaumaturge.research_expertise.title", "研究专长");
        add("research.thaumaturge.research_expertise.stage.1", "一定有一种更高效的方式来处理我的研究笔记。若能再完成一个理论，我确信能找到办法，回收部分所消耗的研究点数。");
        add("research.thaumaturge.research_expertise.stage.2", "你做研究已更为得心应手。<BR>每当你移除放置于六边形中的要素时，有25%%的几率取回该研究点数。");
        add("research.thaumaturge.research_mastery.title", "研究精通");
        add(
                "research.thaumaturge.research_mastery.stage.1",
                "我的造诣已经增长，但对研究过程的真正精通仍与我无缘。更多理论工作应当能带我抵达那里，尽管我担忧，长期浸淫于这些奥秘正对我的头脑产生何种影响。");
        add(
                "research.thaumaturge.research_mastery.stage.2",
                "你做研究已愈发高效。<BR>每当你移除放置于六边形中的要素时，有50%%的几率取回该研究点数。<BR>此外，每当你放置要素时，有10%%的几率不会消耗任何研究点数。<BR>最后，你可以在研究台中通过按住shift点击你想创造的要素来组合要素。若你拥有足够的组成要素，它们便会自动组合成所点击的要素。");
        add("research.thaumaturge.research_duplication.title", "研究拷贝");
        add(
                "research.thaumaturge.research_duplication.stage.1",
                "一份完成的研究笔记会永久保存其图案。只要有足够的研究点数和再一个理论来推演方法，我一定能把它誊抄到新纸上，赠予同僚。");
        add(
                "research.thaumaturge.research_duplication.stage.2",
                "你发现了一种复制已完成研究笔记的方法。<BR>当你完成研究、或将完成的研究笔记放入研究台时，会看到一个星形图标。只要随身携带纸张和墨水，并拥有足够的要素，点击它就能创建一份该研究的副本。<BR>某项研究创建的副本越多，复制它的代价就越高。");
        add("research.thaumaturge.deconstructor.title", "解构工作台");
        add(
                "research.thaumaturge.deconstructor.stage.1",
                "拆解事物、弄清其运转原理，向来是我的拿手好戏。一张专门制作的桌子应当能让我把物体还原为最基本的源质，并从残骸中回收研究点数。");
        add(
                "research.thaumaturge.deconstructor.stage.2",
                "任何神秘使的生涯中都会遇到这样的时刻——由于知识匮乏，研究无法继续推进。<BR>分解台便是可能的出路之一。这张桌子能把物体拆解成你可以检视的最简单部分。但它也有局限——分解台会把复合要素拆解为其组成要素，直到只剩下元始要素。在这一过程中大量知识会流失，神秘使充其量只能指望得到一丁点元始知识。<BR>以铁（金属）为例，<PAGE>它会被拆解为§2地§0与§7秩序§0，而其中只有一种有机会被发现。<BR>这个过程也相当缓慢，物体的要素越少，发现东西的几率就越低。");
        add("gui.thaumaturge.research_table.inspiration", "灵感：%s");
        add("gui.thaumaturge.research_table.draw", "抽取");
        add("gui.thaumaturge.research_table.play", "播放");
        add("gui.thaumaturge.research_table.complete", "完成研究");
        add("gui.thaumaturge.arcane_workbench.vis_available", "可用 %s");
        add("gui.thaumaturge.arcane_workbench.required_vis", "%s 魔力");
        add("gui.thaumaturge.arcane_workbench.required_vis_discount", "%s 魔力（%s%% 减免）");
        add("gui.thaumaturge.arcane_workbench.required_vis_crude", "%s 魔力（未聚焦）");
        add("gui.thaumaturge.arcane_workbench.wand_pay.tooltip", "法杖将代替水晶贡献的元始魔力（每个水晶 %s）");
        add("button.thaumaturge.create_theory", "创造理论");
        add("button.thaumaturge.complete_theory", "完成理论");
        add("button.thaumaturge.scrap_theory", "放弃理论");
        add("block.thaumaturge.research_table", "研究台");
        add("block.thaumaturge.arcane_workbench", "奥术工作台");
        add("block.thaumaturge.arcane_workbench_charger", "奥术工作台充能器");
        add("block.thaumaturge.crucible", "坩埚");
        add("block.thaumaturge.infernal_furnace", "炼狱熔炉");
        add("block.thaumaturge.alembic", "奥术蒸馏器");
        add("block.thaumaturge.bellows", "奥术风箱");
        add("block.thaumaturge.smelter_basic", "源质冶炼厂");
        add("block.thaumaturge.smelter_thaumium", "神秘源质冶炼厂");
        add("block.thaumaturge.smelter_void", "虚空金属源质冶炼厂");
        add("block.thaumaturge.smelter_aux", "附属粘浆泵");
        add("block.thaumaturge.smelter_vent", "辅助排放口");
        add("block.thaumaturge.jar_normal", "源质罐子");
        add("block.thaumaturge.jar_void", "虚空罐子");
        add("item.thaumaturge.thaumonomicon", "魔导手册");
        add("item.thaumaturge.thaumonomicon_cheat", "作弊者魔导手册");
        add("item.thaumaturge.thaumonomicon_sharing", "知识共享之书");
        add("item.thaumaturge.thaumonomicon_linking", "永恒共享之书");
        add("item.thaumaturge.creative_node_placer", "创造节点放置器");
        add("block.thaumaturge.node_transducer", "节点换能器");
        add("block.thaumaturge.vis_relay", "魔力中继器 ");
        add("tooltip.thaumaturge.creative_only", "仅限创造模式");
        add("tooltip.thaumaturge.sharing.bound", "已与 %s 同调");
        add("tooltip.thaumaturge.sharing.hint", "使用一次以同调，然后让你的研究伙伴使用它");
        add("tc.thaumonomicon.cheat.granted", "这本书将 %s 个秘密耳语进你的脑海");
        add("tc.thaumonomicon.sharing.bound", "这本书与你的心灵同调——把它交给你的研究伙伴");
        add("tc.thaumonomicon.sharing.self", "这本书已与你同调——它需要他人的触碰");
        add("tc.thaumonomicon.sharing.linked", "你的知识现在自由地在你与 %s 之间流动");
        add("tc.thaumonomicon.sharing.used", "%s 已将其全部知识传授于你");
        add("tc.elemental_sword.whirlwind_on", "剑刃的风再度翻涌");
        add("tc.elemental_sword.whirlwind_off", "剑刃的风平息了");
        add("tooltip.thaumaturge.elemental_sword.toggle", "潜行+右键以切换旋风");
        add("item.thaumaturge.jar_brace", "黄铜盖箍");
        add("item.thaumaturge.label", "标签");
        add("item.thaumaturge.marked_label", "Marked Label");
        add("item.thaumaturge.salis_mundus", "世界盐");
        add("tooltip.thaumaturge.salis_mundus.desc", "魔法尘埃，施加于特定方块时可触发转化。");
        add("item.thaumaturge.vis_resonator", "魔力谐振器");
        add("item.thaumaturge.fabric", "魔力布匹");
        add("item.thaumaturge.mirrored_glass", "镜面玻璃");
        add("item.thaumaturge.filter", "源质滤管");
        add("item.thaumaturge.mechanism_simple", "简易奥术装置");
        add("item.thaumaturge.mechanism_complex", "精密奥术装置");
        add("item.thaumaturge.morphic_resonator", "形态谐振器");
        add("item.thaumaturge.bath_salts", "浴盐");
        add("item.thaumaturge.sanity_soap", "祛邪肥皂");
        add("block.thaumaturge.spa", "奥术浴场");
        add("block.thaumaturge.purifying_fluid", "净化之水");
        add("fluid_type.thaumaturge.purifying", "净化之水");
        add("block.thaumaturge.liquid_death", "死亡之水");
        add("fluid_type.thaumaturge.liquid_death", "死亡之水");
        add("item.thaumaturge.liquid_death_bucket", "死亡之水桶");
        add("gui.thaumaturge.spa.mix.true", "药浴");
        add("gui.thaumaturge.spa.mix.false", "仅以该液体洗浴");
        add("item.thaumaturge.chunk_beef", "牛肉粒");
        add("item.thaumaturge.chunk_chicken", "鸡肉粒");
        add("item.thaumaturge.chunk_pork", "猪肉粒");
        add("item.thaumaturge.chunk_fish", "鱼肉粒");
        add("item.thaumaturge.chunk_rabbit", "兔肉粒");
        add("item.thaumaturge.chunk_mutton", "羊肉粒");
        add("item.thaumaturge.triple_meat_treat", "三层肉饼");
        add("attributes.thaumaturge.vis_discount", "魔力消耗减免");
        add("subtitles.thaumaturge.jar", "罐子叮当作响");
        add("subtitles.thaumaturge.creak", "管道吱嘎作响");
        add("subtitles.thaumaturge.key", "滤管咔嗒作响");
        add("subtitles.thaumaturge.pump", "源质泵动");
        add("subtitles.thaumaturge.squeek", "阀门吱吱作响");
        add("subtitles.thaumaturge.tool", "管道咔嗒作响");
        add("block.thaumaturge.tube", "源质管道");
        add("block.thaumaturge.tube_valve", "源质阀门");
        add("block.thaumaturge.tube_restrict", "限定源质管道");
        add("block.thaumaturge.tube_filter", "源质过滤管道");
        add("block.thaumaturge.tube_oneway", "定向源质管道");
        add("block.thaumaturge.tube_buffer", "源质缓存器");

        add("item.thaumaturge.alumentum", "源动之焰");
        add("block.thaumaturge.nitor_white", "白色闪耀之光");
        add("block.thaumaturge.nitor_orange", "橙色闪耀之光");
        add("block.thaumaturge.nitor_magenta", "品红色闪耀之光");
        add("block.thaumaturge.nitor_light_blue", "淡蓝色闪耀之光");
        add("block.thaumaturge.nitor_yellow", "黄色闪耀之光");
        add("block.thaumaturge.nitor_lime", "黄绿色闪耀之光");
        add("block.thaumaturge.nitor_pink", "粉色闪耀之光");
        add("block.thaumaturge.nitor_gray", "灰色闪耀之光");
        add("block.thaumaturge.nitor_light_gray", "淡灰色闪耀之光");
        add("block.thaumaturge.nitor_cyan", "青色闪耀之光");
        add("block.thaumaturge.nitor_purple", "紫色闪耀之光");
        add("block.thaumaturge.nitor_blue", "蓝色闪耀之光");
        add("block.thaumaturge.nitor_brown", "棕色闪耀之光");
        add("block.thaumaturge.nitor_green", "绿色闪耀之光");
        add("block.thaumaturge.nitor_red", "红色闪耀之光");
        add("block.thaumaturge.nitor_black", "黑色闪耀之光");

        ResearchTextZhCn.addAll(this::add);

        add("key.thaumaturge.thaumonomicon", "打开魔导手册");
        add("gui.thaumaturge.entry.advance", "标记为已读");
        add("tc.stage.complete", "已完成");
        add("tc.stage.hold", "正在完成……");
        add("tc.research.complete", "研究完成！");
        add("tc.aspect.name", "源质要素");
        add("tc.knowledge.name", "知识总览");
        add("tc.aspect.primal", "元始要素");
        add("tc.aspect.unknown", "未知要素");
        add("tc.aspect.unknown.short", "???");
        add("tc.aspect.unknown.desc", "其性质尚不清楚");
        add("tc.aspect.composition", "%1$s + %2$s");
        add("tc.aspect.progress", "要素已知 %1$s/%2$s");
        add("tc.discoveryerror.derive", "要窥破其中真谛你需要研究 %1$s.");
        add("tc.addendumtext", "§o附录 %1$s§r");

        add("jei.thaumaturge.category.arcane_workbench", "奥术工作台");
        add("jei.thaumaturge.arcane_workbench.vis_cost", "魔力消耗，从当地灵气中汲取");
        add("jei.thaumaturge.arcane_workbench.vis_cost_wand", "插入法杖可聚焦合成：其端帽减免生效，并可用每个所需水晶 %s 魔力代替消耗水晶");
        add("jei.thaumaturge.arcane_workbench.vis_cost_aura", "改用水晶支付将导致未聚焦：%s 魔力");
        add("jei.thaumaturge.category.crucible", "坩埚");
        add("jei.thaumaturge.category.dust_trigger", "世界盐触发");
        add("jei.thaumaturge.category.multiblock_dust_trigger", "多方块结构触发");
        add("jei.thaumaturge.category.aspect_composition", "要素组成");
        add("jei.thaumaturge.category.aspect_from_stacks", "从物品堆提取要素");
        add("jei.thaumaturge.dust_trigger.usage", "用世界盐右键目标方块以触发此转化。");
        add("jei.thaumaturge.dust_trigger.target.tag", "标签 %1$s 中的任意方块");
        add("jei.thaumaturge.dust_trigger.target.multiblock", "右键多方块结构中的任意方块以触发此转化。");
        add("jei.thaumaturge.research.missing_research", "缺少研究：");
        add("tooltip.thaumaturge.aspects.header", "要素：");

        // Resources
        add("block.thaumaturge.ore_amber", "琥珀矿石");
        add("block.thaumaturge.ore_cinnabar", "朱砂矿石");
        add("block.thaumaturge.ore_quartz", "石英矿石");

        add("block.thaumaturge.alchemical_construct", "炼金构材");
        add("block.thaumaturge.advanced_alchemical_construct", "高级炼金构材");

        add("block.thaumaturge.metal_thaumium", "神秘块");
        add("block.thaumaturge.metal_brass", "炼金黄铜块");
        add("block.thaumaturge.metal_void", "虚空金属块");
        add("block.thaumaturge.amber_block", "琥珀方块");

        add("item.thaumaturge.rare_earth", "稀土");

        add("item.thaumaturge.quicksilver", "水银");
        add("item.thaumaturge.amber", "琥珀");
        add("item.thaumaturge.ingot_thaumium", "神秘锭");
        add("item.thaumaturge.ingot_brass", "炼金黄铜锭");
        add("item.thaumaturge.ingot_void", "虚空金属锭");

        add("item.thaumaturge.nugget_quartz", "石英碎片");
        add("item.thaumaturge.nugget_quicksilver", "水银液滴");
        add("item.thaumaturge.nugget_thaumium", "神秘粒");
        add("item.thaumaturge.nugget_brass", "炼金黄铜粒");
        add("item.thaumaturge.nugget_void", "虚空金属粒");

        add("item.thaumaturge.plate_iron", "铁板");
        add("item.thaumaturge.plate_brass", "炼金黄铜板");
        add("item.thaumaturge.plate_thaumium", "神秘板");
        add("item.thaumaturge.plate_void", "虚空金属板");

        add("item.thaumaturge.cluster_iron", "铁原矿簇");
        add("item.thaumaturge.cluster_gold", "金原矿簇");
        add("item.thaumaturge.cluster_copper", "铜原矿簇");
        add("item.thaumaturge.cluster_silver", "银原矿簇");
        add("item.thaumaturge.cluster_lead", "铅原矿簇");
        add("item.thaumaturge.cluster_tin", "锡原矿簇");
        add("item.thaumaturge.cluster_cinnabar", "天然朱砂簇");
        add("item.thaumaturge.cluster_quartz", "石英原矿簇");

        langBCrystals();
        langBaubles();
        langCStone();
        langDTrees();
        langEPlants();
        langMAuraHud();
        langHContainers();
        langTaint();
        langGTools();
        langNScanning();
        langDecor();
        langCasters();
        langEnchantments();
        langGolemancy();
        langEldritch();
        langBosses();
        langConstructs();
        langDecorSweep();
        langOuterLands();
    }

    private void aspect(String tag, String name, String description, String help) {
        add("aspect.thaumaturge." + tag, name);
        add("aspect.thaumaturge." + tag + ".desc", description);
        add("aspect.thaumaturge." + tag + ".help", help);
    }

    private void researchCategory(String path, String name) {
        add("research_category.thaumaturge." + path, name);
    }

    private void card(String path, String name) {
        add("card.thaumaturge." + path + ".name", name);
    }

    private void langBCrystals() {

        add("block.thaumaturge.crystal_aer", "风之结晶");
        add("block.thaumaturge.crystal_ignis", "火之结晶");
        add("block.thaumaturge.crystal_aqua", "水之结晶");
        add("block.thaumaturge.crystal_terra", "地之结晶");
        add("block.thaumaturge.crystal_ordo", "秩序结晶");
        add("block.thaumaturge.crystal_perditio", "混沌结晶");
        add("block.thaumaturge.crystal_vitium", "腐化结晶");
    }

    private void langBaubles() {

        add("item.thaumaturge.amulet_mundane", "凡人护身符");
        add("item.thaumaturge.ring_mundane", "凡人指环");
        add("item.thaumaturge.girdle_mundane", "凡人腰带");
        add("item.thaumaturge.ring_apprentice", "学徒指环");
        add("item.thaumaturge.amulet_fancy", "精致护身符");
        add("item.thaumaturge.ring_fancy", "精致指环");
        add("item.thaumaturge.girdle_fancy", "精致腰带");
        add("item.thaumaturge.amulet_vis", "魔力石");
        add("item.thaumaturge.amulet_vis_crafted", "魔力护身符");
        add("item.thaumaturge.amulet_vis.text", "为护甲、饰品与快捷栏中的物品充能");
        add("item.thaumaturge.charm_undying", "不死护符");
        add("item.thaumaturge.cloud_ring", "云霄指环");
        add("item.thaumaturge.curiosity_band", "求知发带");
        add("item.thaumaturge.verdant_charm", "翠心指环");
        add("item.thaumaturge.verdant_charm.life.text", "再生");
        add("item.thaumaturge.verdant_charm.sustain.text", "续航");
        add("item.thaumaturge.voidseer_charm", "虚空预言者的珍珠");
        add("item.thaumaturge.voidseer_charm.text", "我窥探着这漆黑的深渊……似乎它回以了凝视……");
        add("item.thaumaturge.focus_pouch", "施术核心手袋");
        add("item.thaumaturge.sanity_checker", "神智检测仪");
        add("item.thaumaturge.curio_arcane", "奥术珍品");
        add("item.thaumaturge.curio_preserved", "传世珍品");
        add("item.thaumaturge.curio_ancient", "远古珍品");
        add("item.thaumaturge.curio_eldritch", "邪术珍品");
        add("item.thaumaturge.curio_knowledge", "光明珍品");
        add("item.thaumaturge.curio_twisted", "扭曲珍品");
        add("item.thaumaturge.curio_rites", "血腥仪式");
        add("tc.knowledge.gained", "你已经获得了一些知识。");
        add("block.thaumaturge.mirror", "魔镜");
        add("block.thaumaturge.mirror_essentia", "源质之镜");
        add("item.thaumaturge.hand_mirror", "魔力手镜");
        add("tc.handmirrorlinked", "链接已建立。");
        add("tc.handmirrorerror", "目标魔镜遗失或被重置，链接损坏。");
        add("tc.handmirrorlinkedto.full", "已连接至 %s 的 %s,%s,%s");
        add("tc.mirrorlinkedalready", "那面魔镜已连接至有效目标。");
        add("fail.crimsonrites", "这本书里不过只有一些胡言乱语。");
        add("item.thaumaturge.creative_flux_sponge", "创造咒波海绵");
        add("tooltip.thaumaturge.flux_sponge.drain.0", "右键以吸取所有");
        add("tooltip.thaumaturge.flux_sponge.drain.1", "来自9x9区块区域的咒波");
        add("tooltip.thaumaturge.flux_sponge.rifts.0", "也会移除咒波裂隙");
        add("tooltip.thaumaturge.flux_sponge.rifts.1", "如果潜行时使用。");
        add("tooltip.thaumaturge.flux_sponge.creative", "仅限创造模式");
        add("tc.flux_sponge.drained", "已从81个区块中吸取 %s 咒波。");
        add("tc.flux_sponge.rifts", "已移除 %s 个咒波裂隙。");
        add("item.thaumaturge.resonator", "源质谐振器");
        add("item.thaumaturge.fortress_helm", "神秘要塞兜鍪");
        add("item.thaumaturge.fortress_chest", "神秘要塞胸铠");
        add("item.thaumaturge.fortress_legs", "神秘要塞护胫");
        add("item.thaumaturge.fortress_helm.mask.0", "狞笑恶魔面具");
        add("item.thaumaturge.fortress_helm.mask.1", "暴怒幽魂面具");
        add("item.thaumaturge.fortress_helm.mask.2", "嗜血邪妖面具");
        add("item.thaumaturge.void_robe_helm", "虚空神秘使兜帽");
        add("item.thaumaturge.void_robe_chest", "虚空神秘使法袍");
        add("item.thaumaturge.void_robe_legs", "虚空神秘使护腿");
        add("tc.resonator1", "§9包含%1$s%2$s源质§0");
        add("tc.resonator2", "§5吸力%1$s%2$s§0");
        add("tc.resonator3", "未绑定类型");
        add("tc.condenser1", "§d消耗：%1$s 要素§0");
        add("tc.condenser2", "§d时间：%1$s 刻（%2$s 秒）§0");
    }

    private void langCStone() {

        add("block.thaumaturge.stone_arcane", "奥术石材");
        add("block.thaumaturge.stone_arcane_brick", "奥术石砖");
        add("block.thaumaturge.stone_ancient", "荒古石");
        add("block.thaumaturge.stone_ancient_tile", "荒古砖块");
        add("block.thaumaturge.stone_ancient_rock", "荒古岩");
        add("block.thaumaturge.stone_ancient_glyphed", "雕文石头");
        add("block.thaumaturge.stone_ancient_doorway", "荒古门廊");
        add("block.thaumaturge.stone_eldritch_tile", "邪术石");
        add("block.thaumaturge.stone_porous", "透水石");
        add("block.thaumaturge.stairs_arcane", "奥术楼梯");
        add("block.thaumaturge.stairs_arcane_brick", "奥术石砖楼梯");
        add("block.thaumaturge.stairs_ancient", "荒古楼梯");
    }

    private void langDTrees() {

        add("block.thaumaturge.sapling_greatwood", "宏伟之木树苗");
        add("block.thaumaturge.sapling_silverwood", "银树树苗");
        add("block.thaumaturge.log_greatwood", "宏伟之木原木");
        add("block.thaumaturge.log_silverwood", "银树原木");
        add("block.thaumaturge.greatwood", "宏伟之木");
        add("block.thaumaturge.silverwood", "银树");
        add("block.thaumaturge.stripped_log_greatwood", "去皮宏伟之木原木");
        add("block.thaumaturge.stripped_log_silverwood", "去皮银树原木");
        add("block.thaumaturge.stripped_greatwood", "去皮宏伟之木");
        add("block.thaumaturge.stripped_silverwood", "去皮银树");
        add("block.thaumaturge.leaves_greatwood", "宏伟之木树叶");
        add("block.thaumaturge.leaves_silverwood", "银树树叶");
        add("block.thaumaturge.plank_greatwood", "宏伟之木木板");
        add("block.thaumaturge.plank_silverwood", "银树木板");
    }

    private void langEPlants() {

        add("block.thaumaturge.shimmerleaf", "水银花");
        add("block.thaumaturge.cinderpearl", "烈焰草");
        add("block.thaumaturge.vishroom", "纤毛菇");
        add("block.thaumaturge.grass_ambient", "蕴魔草方块");
        add("biome.thaumaturge.magical_forest", "魔法森林");
        add("biome.thaumaturge.eerie", "凶险之地");
        add("biome.thaumaturge.eldritch", "外域");
    }

    private void langMAuraHud() {

        add("item.thaumaturge.goggles_revealing", "揭示之护目镜");
    }

    private void langHContainers() {

        add("item.thaumaturge.phial.empty", "玻璃安瓿");
        add("item.thaumaturge.phial.filled", "%1$s源质安瓿");
        add("item.thaumaturge.phial.unknown", "未知源质安瓿");
        add("item.thaumaturge.primordial_pearl.pearl", "元始珍珠");
        add("item.thaumaturge.primordial_pearl.nodule", "元始结核");
        add("item.thaumaturge.primordial_pearl.mote", "元始尘埃");
    }

    private void langTaint() {

        add("block.thaumaturge.flux_goo", "咒波黏浆");
        add("fluid_type.thaumaturge.flux_goo", "咒波黏浆");

        add("block.thaumaturge.taint_rock", "腐化岩石");
        add("block.thaumaturge.taint_soil", "腐化泥土");
        add("block.thaumaturge.taint_crust", "腐化泥土");
        add("block.thaumaturge.taint_geyser", "腐化喷泉");
        add("block.thaumaturge.taint_log", "腐化原木");
        add("block.thaumaturge.taint_feature", "腐化肿泡");
        add("block.thaumaturge.taint_fibre", "腐化草丛");

        add("effect.thaumaturge.vis_exhaust", "咒流");
        add("effect.thaumaturge.infectious_vis_exhaust", "咒喰");
        add("effect.thaumaturge.flux_taint", "咒波腐化");

        add("death.attack.thaumaturge.taint", "%1$s被腐化了");
        add("death.attack.thaumaturge.tentacle", "%1$s被触手勒死了");
        add("death.attack.thaumaturge.swarm", "%1$s被蜂拥围困");
        add("death.attack.thaumaturge.dissolve", "%1$s溶解了");

        add("item.thaumaturge.essentia_crystal", "%s 魔力水晶碎片");
        add("item.thaumaturge.mana_bean", "魔豆");
        add("block.thaumaturge.mana_pod", "魔力荚");
        add("item.thaumaturge.essentia_crystal.unknown", "未知魔力水晶碎片");

        add("entity.thaumaturge.thaumic_slime", "神秘史莱姆");
        add("entity.thaumaturge.taint_seed", "腐化孢子");
        add("entity.thaumaturge.taint_seed_prime", "巨型腐化孢子");
        add("entity.thaumaturge.taint_crawler", "腐化魔蛛");
        add("entity.thaumaturge.taint_swarm", "腐化孢子群");
        add("entity.thaumaturge.taintacle", "腐化触手怪");
        add("entity.thaumaturge.taintacle_small", "小型腐化触手");
        add("entity.thaumaturge.falling_taint", "腐化坠块");
        add("entity.thaumaturge.bottle_taint", "腐化粘液瓶");
        add("item.thaumaturge.bottle_taint", "腐化瓶");
        add("entity.thaumaturge.wisp", "精灵");
        add("entity.thaumaturge.brainy_zombie", "红眼僵尸");
        add("entity.thaumaturge.giant_brainy_zombie", "红眼僵尸巨人");
        add("entity.thaumaturge.firebat", "九狱焱蝠");
        add("entity.thaumaturge.mind_spider", "心魔蜘蛛");
        add("item.thaumaturge.brain", "僵尸之脑");

        add("item.thaumaturge.brainy_zombie_spawn_egg", "红眼僵尸刷怪蛋");
        add("item.thaumaturge.giant_brainy_zombie_spawn_egg", "红眼僵尸巨人刷怪蛋");
        add("item.thaumaturge.firebat_spawn_egg", "九狱焱蝠刷怪蛋");
        add("item.thaumaturge.mind_spider_spawn_egg", "心魔蜘蛛刷怪蛋");
        add("item.thaumaturge.wisp_spawn_egg", "精灵刷怪蛋");
        add("item.thaumaturge.thaumic_slime_spawn_egg", "神秘史莱姆刷怪蛋");
        add("item.thaumaturge.taint_crawler_spawn_egg", "腐化魔蛛刷怪蛋");
        add("item.thaumaturge.taintacle_spawn_egg", "腐化触手怪刷怪蛋");
        add("item.thaumaturge.taint_swarm_spawn_egg", "腐化孢子群刷怪蛋");
        add("item.thaumaturge.taint_seed_spawn_egg", "腐化孢子刷怪蛋");
        add("item.thaumaturge.taint_seed_prime_spawn_egg", "巨型腐化孢子刷怪蛋");
    }

    private void langGTools() {

        add("item.thaumaturge.thaumometer", "魔导透镜");
        add("item.thaumaturge.scribing_tools", "笔与墨");
    }

    private void langNScanning() {

        add("tc.unknownobject", "您一无所获。");
        add("tc.knownobject", "您学到了一些东西。");
        add("tc.invtoolarge", "容器太大了，仅扫描前100个项目。");
        add("tc.celestial.fail.1", "你今天已经做过同样的研究了。");
        add("tc.celestial.fail.2", "你无法记录下你的研究笔记。");
        add("tc.celestial.studied", "你将天象观测记入脑海。");
        add(
                "jei.thaumaturge.deconstruction.info",
                "将任何带有要素的物品放在分解台上，它会缓慢地将物品分解。每个工作周期都有机会释放出一个随机元始要素的研究点数；物品的要素越丰富，几率越高。点击漂浮的要素即可收集。");

        add("item.thaumaturge.celestial_notes", "天象笔记");
        add("item.thaumaturge.celestial_notes.sun.text", "太阳");
        add("item.thaumaturge.celestial_notes.stars_1.text", "星象，北方玄武");
        add("item.thaumaturge.celestial_notes.stars_2.text", "星象，南方朱雀");
        add("item.thaumaturge.celestial_notes.stars_3.text", "星象，西方白虎");
        add("item.thaumaturge.celestial_notes.stars_4.text", "星象，东方青龙");
        add("item.thaumaturge.celestial_notes.moon_1.text", "月亮，满月");
        add("item.thaumaturge.celestial_notes.moon_2.text", "月亮，亏凸月");
        add("item.thaumaturge.celestial_notes.moon_3.text", "月亮，下弦月");
        add("item.thaumaturge.celestial_notes.moon_4.text", "月亮，残月");
        add("item.thaumaturge.celestial_notes.moon_5.text", "月亮，新月");
        add("item.thaumaturge.celestial_notes.moon_6.text", "月亮，娥眉月");
        add("item.thaumaturge.celestial_notes.moon_7.text", "月亮，上弦月");
        add("item.thaumaturge.celestial_notes.moon_8.text", "月亮，盈凸月");

        add("card.thaumaturge.curio.name", "研究珍品");
        add("card.thaumaturge.curio.text", "你在研究珍品时往往会揭示一些有趣的信息，它可以作为一个很好的对照研究对象。");
        add("card.thaumaturge.enchantment.name", "研究附魔");
        add(
                "card.thaumaturge.enchantment.text",
                "你通过研究基础附魔来从根本探寻它运作的方式。你相信在注魔和炼化学当中的附魔与之有许多共通之处。你失去5个经验等级，以换取15到20点 注魔和炼化学 的进度。");
        add("card.thaumaturge.beacon.name", "灵气作用");
        add(
                "card.thaumaturge.beacon.text",
                "你相信信标能够以某种方式影响周围神秘的灵气。你仔细地进行研究，虽然没有取得十足的进展，但也从中受到了启发。你获得了2点灵感与一次额外的抽卡机会，并且将会有多个额外的分支在完成时获得全额奖励。 ");
        add("card.thaumaturge.dragonegg.name", "龙之研究");
        add("card.thaumaturge.dragonegg.text", "龙蛋周围萦绕着强大的灵力与混沌的能量，偶尔你也能在其中看到混沌的秩序。");
        add("card.thaumaturge.concentrate.name", "浓缩");
        add(
                "card.thaumaturge.concentrate.text",
                "通过将物质浓缩成最纯净的形式，你能够学到许多知识。尝试浓缩%1$s源质，获得15点 炼金术 进度并获得一次额外的抽卡机会。还有机会获得1点灵感。");
        add("card.thaumaturge.reactions.name", "反应");
        add(
                "card.thaumaturge.reactions.text",
                "研究两种不同的Vis之间如何反应也有所裨益。你应当研究%1$s源质如何与%2$s源质进行反应。获得25点 炼金术 进度。还有机会获得1点灵感。");
        add("card.thaumaturge.synthesis.name", "合成");
        add(
                "card.thaumaturge.synthesis.text",
                "当源质相互结合形成更复杂的形式时，总会产生许多有趣和复杂的反应。你通过研究%1$s与%2$s源质的结合学到了很多知识。获得40点 炼金术 进度。还有机会获得1点灵感。");
        add("card.thaumaturge.measure.name", "测量");
        add("card.thaumaturge.measure.text", "你花费大量时间详细测量不同种类的源质以及它们所蕴含的Vis。获得15点 注魔 进度并获得一次额外的备选研究。");
        add("card.thaumaturge.channel.name", "引导 %1$s 源质");
        add("card.thaumaturge.channel.text", "你通过一些简单的实验来测试注魔过程当中引导 %1$s 源质会发生什么。获得25点 注魔 进度。");
        add("card.thaumaturge.infuse.name", "注魔实验");
        add(
                "card.thaumaturge.infuse.text",
                "通过假定使用源质注魔特定物体的效果，并对成果进行测试，你获得了宝贵的见解。你通过将%1$s源质与%2$s进行结合并研究其成果，学到了很多知识。获得%3$s点 注魔 进度。");
        add("card.thaumaturge.calibrate.name", "校准");
        add("card.thaumaturge.calibrate.text", "你花了一些时间来校准你的仪器与工具。获得15点 炼化学 进度并获得一次额外的抽卡机会。");
        add("card.thaumaturge.mindmatter.name", "意识超脱于物质");
        add("card.thaumaturge.mindmatter.text", "你仔细地检查拆分了一些基本组件，寻求将他们组装成更复杂的设计的方法。获得%1$s点 炼化学 进度。");
        add("card.thaumaturge.tinker.name", "调整");
        add("card.thaumaturge.tinker.text", "你开始调整一些设备，希望能够找到新的方式将其纳入到自己的魔法创造当中。获得%1$s到%2$s点 炼化学 进度。");
        add("card.thaumaturge.spellbinding.name", "结合法术");
        add(
                "card.thaumaturge.spellbinding.text",
                "你在剩下的小块结晶中测试不同的附魔。漫无目的的尝试给你带来了宝贵的知识。失去最多5个经验等级，每失去一个等级将会为你带来5点 神秘学 进度。");
        add("card.thaumaturge.awareness.name", "认知");
        add(
                "card.thaumaturge.awareness.text",
                "你置身于Vis的流动之中，你对它如何运作以及事物的基本性质有了更深入的了解，但这也让你的精神变得脆弱。获得20点 神秘学 进度。同时你可能会获得 邪术学 的知识并获得扭曲。");
        add("card.thaumaturge.focus.name", "全神贯注");
        add("card.thaumaturge.focus.text", "你将注意力集中在周围的魔法和精神能量上，希望能够与它们的波动相协调。获得15点 神秘学 进度并获得一次额外的抽卡机会。");
        add("card.thaumaturge.sculpting.name", "雕塑");
        add("card.thaumaturge.sculpting.text", "通过创造简单的短时活动塑像，你磨练了你的知识。获得20点 傀儡学 进度，并获得一次额外的抽卡机会。");
        add("card.thaumaturge.scripting.name", "脚本编写");
        add(
                "card.thaumaturge.scripting.text",
                "傀儡学很大一部分是创造复杂的奥术文字来操控你的造物。通过创建一些测试脚本，你增进了你的学识。获得25点 傀儡学 进度。这会额外消耗研究台上的墨水与纸张。");
        add("card.thaumaturge.synergy.name", "协同");
        add(
                "card.thaumaturge.synergy.text",
                "从根源来说，傀儡学是炼金术、炼化学与注魔的结合。只有充分理解这三个分支如何相互作用，才能够真正精通傀儡学。 总共失去15点进度，平均分配于 炼金术、炼化学与注魔 方面，同时获得30点 傀儡学 进度，并且将会有一个额外的分支在完成时获得全额奖励。");
        add("card.thaumaturge.darkwhisper.name", "黑暗低语");
        add("card.thaumaturge.darkwhisper.text", "最近缸中之脑总是口若悬河，它承诺将所有古老的秘密都告诉你。你能相信它么？");
        add("card.thaumaturge.glyph.name", "研究雕纹");
        add("card.thaumaturge.glyph.text", "你研究古代雕纹，不知其中蕴含了什么邪术学的奥秘。这些古代的语言晦涩难懂，但一个个细碎的事实正将它揭示于你。");
        add("card.thaumaturge.portal.name", "异界回响");
        add("card.thaumaturge.portal.text", "笼罩在传送门发出的诡异光线中，诡异的思绪侵入你的大脑。其中大部分的内容都难以理解，但也有些为你带来了奇怪的灵感。");
        add("card.thaumaturge.revelation.name", "启示");
        add(
                "card.thaumaturge.revelation.text",
                "研究邪术学的道路充满了未知的危险，但为了所揭示的学识往往是值得的。你获得25点 邪术学 进度并获得5点随机分支的进度。同时你也会获得一些临时扭曲与粘性扭曲，并且将会有一个额外的分支在完成时获得全额奖励。");
        add("card.thaumaturge.realization.name", "顿悟");
        add(
                "card.thaumaturge.realization.text",
                "在探寻邪术学的本质时，你突然对天地万物的本质有了惊人的见解。你获得15点 邪术学 进度并获得5到10点两个随机分支的进度。 同时你也会获得一些临时扭曲。你也有可能会获得一些粘性扭曲。");
        add("card.thaumaturge.truth.name", "寻求真相");
        add("card.thaumaturge.truth.text", "你拼命寻求邪术学背后的真相以及它对于世界的意义。你获得10到25点 邪术学 进度并获得一次额外的抽卡机会。 同时你也会获得一些临时扭曲。");
        add("card.thaumaturge.celestial.name", "天象研究");
        add("card.thaumaturge.celestial.text", "你拿一些你记录的天象笔记，并将它们与你的主要研究分支进行比对。你在 %1$s 获得25到50点进度的启发。你同时也可能获得其他的东西……");

        add("research.thaumaturge.flux.warn", "灵气场中充满了一种力量，一种不太好的力量。");
        add("research.thaumaturge.warp.warn", "我所掌握的知识似乎让我有些神志不清");
        add("research.thaumaturge.oculus.title", "眼");
        add(
                "research.thaumaturge.oculus.stage_0",
                "低语已汇聚成合唱，我终于明白它们想让我做什么。散布于世界各处的方尖碑并非纪念碑——它们是门，而每扇门都有钥匙。<BR>我最初遭遇血腥邪徒的那些怪异祭坛上，有一块标有四个空凹槽的拱顶石。四只眼睛必须安放其中——或制作，或交易得来——拱顶石上方的凶险能量则必须保持完好。<BR>在尝试如此鲁莽之事前，我应当先理清自己的理论。");
        add(
                "research.thaumaturge.oculus.stage_1",
                "一切都那么简单——我惊讶于血腥邪徒竟然从未发现这一点。<BR>把四只邪术之眼安放在拱顶石上，再以法杖将一股聚焦的魔力注入祭坛。当地灵气付出代价，所谓的\"眼\"便就此睁开。<BR>当然，我完全不知道那意味着什么。无妨——只有傻瓜才会畏惧未知！");
        add("research.thaumaturge.enter_outer_lands.title", "外域");
        add(
                "research.thaumaturge.enter_outer_lands.stage_0",
                "通过开眼仪式时你并不十分确定你究竟在期待什么,但肯定不是眼前这些碎裂石块建成的诡异建筑和这些七拐八弯的扭曲通道.<BR>这里有些不对劲 - 这些建筑物看上去除了构成巨大的致命迷宫外别无设计目的.<BR>四周涌动着诡异的能量波动,你的魔力在这异域的环境中似乎表现得有些异常.即使你遇到的其他生物看上去也给你一种格格不入的诡异感觉.");
        add("research.thaumaturge.outer_revelations.title", "外域启示");
        add(
                "research.thaumaturge.outer_revelations.stage_0",
                "你所疑虑的事情得到了证实.这里并非你打算命名为外域人的种族的家园.这完全是另一个地方.你并不相信它存在于你所理解的\"现实\"当中 - 相比实在的事物它更接近某种精神上的结构,但什么样的精神能够容纳它呢?<BR>你仅仅能破译出少量的符文,但你确信这地方是一个陷阱,一个用于考验来客并淘汰掉弱者的地方.出于何种目的你还不得而知.");
        add("gui.thaumaturge.altar.ritual_unknown", "拱顶石嗡鸣作响，但它的用途暂时超出了你的理解……");
    }

    private void langDecor() {

        add("block.thaumaturge.dioptra", "神秘屈光仪");
        add("block.thaumaturge.vis_battery", "灵气电池");
        add("block.thaumaturge.matrix_speed", "注魔加速石");
        add("block.thaumaturge.matrix_cost", "注魔效能石");
        add("block.thaumaturge.jar_brain", "缸中之脑");
        add("block.thaumaturge.arcane_ear", "奥术之耳");
        add("block.thaumaturge.arcane_ear_toggle", "奥术之耳（切换）");
        add("block.thaumaturge.lamp_arcane", "奥术灯");
        add("block.thaumaturge.lamp_growth", "催生灯");
        add("block.thaumaturge.lamp_fertility", "育种灯");
        add("block.thaumaturge.centrifuge", "源质离心机");
        add("block.thaumaturge.hungry_chest", "饕餮箱子");
        add("block.thaumaturge.everfull_urn", "无尽之瓮");
        add("block.thaumaturge.vis_generator", "灵气发电机");
        add("block.thaumaturge.essentia_input", "源质析出装置");
        add("block.thaumaturge.essentia_output", "源质注入装置");
        add("block.thaumaturge.condenser", "咒波凝结器");
        add("block.thaumaturge.condenser_lattice", "咒波凝结器格栅");
        add("block.thaumaturge.condenser_lattice_dirty", "堵塞的咒波凝结器格栅");
        add("block.thaumaturge.stabilizer", "稳定器");
        add("block.thaumaturge.redstone_relay", "红石继能器");
        add("block.thaumaturge.void_siphon", "虚空吸管");
        add("block.thaumaturge.thaumatorium", "神秘炼金塔");
        add("block.thaumaturge.thaumatorium_top", "神秘炼金塔");
        add("block.thaumaturge.brain_box", "记忆矩阵");
        add("item.thaumaturge.tallow", "魔力油脂");
        add("block.thaumaturge.placeholder_obsidian", "炼狱熔炉");
        add("block.thaumaturge.placeholder_nether_bricks", "炼狱熔炉");
        add("item.thaumaturge.warping", "扭曲");
        add("effect.thaumaturge.thaumarhia", "玄癔");
        add("effect.thaumaturge.unnatural_hunger", "反常之饥");
        add("effect.thaumaturge.sun_scorned", "日之辱蔑");
        add("effect.thaumaturge.death_gaze", "死亡凝视");
        add("effect.thaumaturge.blurred_vision", "视瞻昏渺");
        add("effect.thaumaturge.warp_ward", "扭曲守护");
        add("warp.thaumaturge.gain.permanent", "你获得了永久扭曲！");
        add("warp.thaumaturge.gain.normal", "你获得了扭曲！");
        add("warp.thaumaturge.gain.temporary", "你获得了临时扭曲！");
        add("warp.thaumaturge.lose.permanent", "你的永久扭曲消退了！");
        add("warp.thaumaturge.lose.normal", "你的扭曲消退了！");
        add("warp.thaumaturge.lose.temporary", "你的临时扭曲消退了！");
        add("warp.thaumaturge.text.1", "你身上涌出古怪的疲乏感。");
        add("warp.thaumaturge.text.2", "突如其来的反常饥饿感让你感到劳累。");
        add("warp.thaumaturge.text.3", "古怪的耳语向你倾吐着秘密。");
        add("warp.thaumaturge.text.4", "你的视野突然变得诡异而模糊。");
        add("warp.thaumaturge.text.5", "光线突然变得明亮起来，不可抗拒，你的皮肤开始燃烧。");
        add("warp.thaumaturge.text.6", "浓雾凭空而生，其间鬼影绰绰。");
        add("warp.thaumaturge.text.7", "它们到处都是！快跑！");
        add("warp.thaumaturge.text.8", "肯定有什么办法可以遏制这些头疼事！");
        add("warp.thaumaturge.text.9", "突然间，你厌倦了破坏。");
        add("warp.thaumaturge.text.10", "你的感觉猛地膨胀开来。");
        add("warp.thaumaturge.text.11", "那片嘈杂声是怎么回事？有什么东西在尾随着你……");
        add("warp.thaumaturge.text.12", "有什么东西在尾随着你……");
        add("warp.thaumaturge.text.13", "冥冥之中的一道目光在注视着你，也许是时候罢手了。");
        add("warp.thaumaturge.text.14", "有那么一瞬间，你感到头脑无比清晰。");
        add("warp.thaumaturge.text.15", "你的胃突然发出异常古怪的咕咕声。");
        add("warp.thaumaturge.text.16", "那微弱的咏唱声在附近依稀可闻。");
        add("warp.thaumaturge.text.hunger.1", "平常的食物似乎无法抑制你的饥饿感。");
        add("warp.thaumaturge.text.hunger.2", "你的饥饿感开始消退。");
        add("warp.thaumaturge.fluxevent.2", "你感到有什么东西侵入你的脑海，正在蚕食你的意志。");
        add("warp.thaumaturge.fluxevent.3", "你感到附近的魔力张力骤然释放。");
        add("entity.thaumaturge.flux_rift", "咒波裂隙");
        add("item.thaumaturge.void_seed", "虚空种子");
        add("item.thaumaturge.causality_collapser", "悖论物质");
        add("block.thaumaturge.infusion_matrix", "符文矩阵");
        add("block.thaumaturge.pedestal_arcane", "奥术基座");
        add("block.thaumaturge.recharge_pedestal", "充能基座");
        add("block.thaumaturge.pedestal_ancient", "荒古基座");
        add("block.thaumaturge.pedestal_eldritch", "邪术基座");
        add("block.thaumaturge.pillar_arcane", "注魔柱");
        add("block.thaumaturge.pillar_ancient", "荒古注魔柱");
        add("block.thaumaturge.pillar_eldritch", "邪术注魔柱");
        add("gui.thaumaturge.infusion.instability", "不稳定度：%s");
        add("gui.thaumaturge.infusion.instability.0", "可忽略");
        add("gui.thaumaturge.infusion.instability.1", "略有风险");
        add("gui.thaumaturge.infusion.instability.2", "稍有危险");
        add("gui.thaumaturge.infusion.instability.3", "高");
        add("gui.thaumaturge.infusion.instability.4", "极高");
        add("gui.thaumaturge.infusion.instability.5", "危险");
        add("gui.thaumaturge.infusion.stability.very_stable", "非常稳定");
        add("gui.thaumaturge.infusion.stability.stable", "稳定");
        add("gui.thaumaturge.infusion.stability.unstable", "不稳定");
        add("gui.thaumaturge.infusion.stability.very_unstable", "极具危险");
        add("gui.thaumaturge.infusion.stability.gain", "每周期增益");
        add("gui.thaumaturge.infusion.stability.range", "0 至 ");
        add("gui.thaumaturge.infusion.stability.loss", "每周期损失");
        add("entity.thaumaturge.causality_collapser", "悖论物质");
        add("item.thaumaturge.thaumium_sword", "神秘剑");
        add("item.thaumaturge.thaumium_pickaxe", "神秘镐");
        add("item.thaumaturge.thaumium_axe", "神秘斧");
        add("item.thaumaturge.thaumium_shovel", "神秘锹");
        add("item.thaumaturge.thaumium_hoe", "神秘锄");
        add("item.thaumaturge.thaumium_helm", "神秘头盔");
        add("item.thaumaturge.thaumium_chest", "神秘胸甲");
        add("item.thaumaturge.thaumium_legs", "神秘护胫");
        add("item.thaumaturge.thaumium_boots", "神秘靴子");
        add("item.thaumaturge.void_sword", "虚空剑");
        add("item.thaumaturge.void_pickaxe", "虚空镐");
        add("item.thaumaturge.void_axe", "虚空斧");
        add("item.thaumaturge.void_shovel", "虚空锹");
        add("item.thaumaturge.void_hoe", "虚空锄");
        add("item.thaumaturge.void_helm", "虚空头盔");
        add("item.thaumaturge.void_chest", "虚空胸甲");
        add("item.thaumaturge.void_legs", "虚空护胫");
        add("item.thaumaturge.void_boots", "虚空靴子");
        add("item.thaumaturge.elemental_sword", "风雷剑");
        add("item.thaumaturge.elemental_pickaxe", "炽心镐");
        add("item.thaumaturge.elemental_axe", "奔流斧");
        add("item.thaumaturge.elemental_shovel", "后土铲");
        add("item.thaumaturge.elemental_hoe", "句芒锄");
        add("item.thaumaturge.primal_crusher", "元始杵");
        add("item.thaumaturge.traveller_boots", "旅行者之靴");
        add("item.thaumaturge.cloth_chest", "学徒法袍");
        add("item.thaumaturge.cloth_legs", "学徒护腿");
        add("item.thaumaturge.cloth_boots", "学徒之靴");

        add("block.thaumaturge.candle_white", "白色油脂蜡烛");
        add("block.thaumaturge.banner_white", "白色旗帜");
        add("block.thaumaturge.wall_banner_white", "白色旗帜");
        add("block.thaumaturge.candle_orange", "橙色油脂蜡烛");
        add("block.thaumaturge.banner_orange", "橙色旗帜");
        add("block.thaumaturge.wall_banner_orange", "橙色旗帜");
        add("block.thaumaturge.candle_magenta", "品红色油脂蜡烛");
        add("block.thaumaturge.banner_magenta", "品红色旗帜");
        add("block.thaumaturge.wall_banner_magenta", "品红色旗帜");
        add("block.thaumaturge.candle_light_blue", "淡蓝色油脂蜡烛");
        add("block.thaumaturge.banner_light_blue", "淡蓝色旗帜");
        add("block.thaumaturge.wall_banner_light_blue", "淡蓝色旗帜");
        add("block.thaumaturge.candle_yellow", "黄色油脂蜡烛");
        add("block.thaumaturge.banner_yellow", "黄色旗帜");
        add("block.thaumaturge.wall_banner_yellow", "黄色旗帜");
        add("block.thaumaturge.candle_lime", "黄绿色油脂蜡烛");
        add("block.thaumaturge.banner_lime", "黄绿色旗帜");
        add("block.thaumaturge.wall_banner_lime", "黄绿色旗帜");
        add("block.thaumaturge.candle_pink", "粉色油脂蜡烛");
        add("block.thaumaturge.banner_pink", "粉色旗帜");
        add("block.thaumaturge.wall_banner_pink", "粉色旗帜");
        add("block.thaumaturge.candle_gray", "灰色油脂蜡烛");
        add("block.thaumaturge.banner_gray", "灰色旗帜");
        add("block.thaumaturge.wall_banner_gray", "灰色旗帜");
        add("block.thaumaturge.candle_light_gray", "淡灰色油脂蜡烛");
        add("block.thaumaturge.banner_light_gray", "淡灰色旗帜");
        add("block.thaumaturge.wall_banner_light_gray", "淡灰色旗帜");
        add("block.thaumaturge.candle_cyan", "青色油脂蜡烛");
        add("block.thaumaturge.banner_cyan", "青色旗帜");
        add("block.thaumaturge.wall_banner_cyan", "青色旗帜");
        add("block.thaumaturge.candle_purple", "紫色油脂蜡烛");
        add("block.thaumaturge.banner_purple", "紫色旗帜");
        add("block.thaumaturge.wall_banner_purple", "紫色旗帜");
        add("block.thaumaturge.candle_blue", "蓝色油脂蜡烛");
        add("block.thaumaturge.banner_blue", "蓝色旗帜");
        add("block.thaumaturge.wall_banner_blue", "蓝色旗帜");
        add("block.thaumaturge.candle_brown", "棕色油脂蜡烛");
        add("block.thaumaturge.banner_brown", "棕色旗帜");
        add("block.thaumaturge.wall_banner_brown", "棕色旗帜");
        add("block.thaumaturge.candle_green", "绿色油脂蜡烛");
        add("block.thaumaturge.banner_green", "绿色旗帜");
        add("block.thaumaturge.wall_banner_green", "绿色旗帜");
        add("block.thaumaturge.candle_red", "红色油脂蜡烛");
        add("block.thaumaturge.banner_red", "红色旗帜");
        add("block.thaumaturge.wall_banner_red", "红色旗帜");
        add("block.thaumaturge.candle_black", "黑色油脂蜡烛");
        add("block.thaumaturge.banner_black", "黑色旗帜");
        add("block.thaumaturge.wall_banner_black", "黑色旗帜");
        add("block.thaumaturge.banner_crimson_cult", "血腥教徒旗帜");
        add("block.thaumaturge.wall_banner_crimson_cult", "血腥教徒旗帜");
    }

    private void langCasters() {

        add("key.category.thaumaturge.main", "Thaumaturge");
        add("key.thaumaturge.change_focus", "切换法杖核心");
        add("key.thaumaturge.misc_toggle", "法杖杂项切换");
        add("item.thaumaturge.wand", "法杖");
        add("item.thaumaturge.wand.named", "%1$s%2$s法杖");
        add("item.thaumaturge.wand.sceptre", "%1$s%2$s权杖");
        add("item.thaumaturge.wand.staff", "%1$s%2$s手杖");
        add("wand.thaumaturge.cap.iron", "铁杖端");
        add("wand.thaumaturge.cap.copper", "铜杖端");
        add("wand.thaumaturge.cap.gold", "金杖端");
        add("wand.thaumaturge.cap.silver", "银杖端");
        add("wand.thaumaturge.cap.thaumium", "神秘杖端");
        add("wand.thaumaturge.cap.void", "虚空杖端");
        add("wand.thaumaturge.rod.wood", "木质");
        add("wand.thaumaturge.rod.greatwood", "宏伟之木");
        add("wand.thaumaturge.rod.silverwood", "银树");
        add("wand.thaumaturge.rod.obsidian", "黑曜石");
        add("wand.thaumaturge.rod.blaze", "烈焰");
        add("wand.thaumaturge.rod.ice", "寒冰");
        add("wand.thaumaturge.rod.bone", "白骨");
        add("wand.thaumaturge.rod.reed", "甘蔗");
        add("wand.thaumaturge.rod.quartz", "石英");
        add("wand.thaumaturge.rod.primal", "元始");
        add("tooltip.thaumaturge.focus_pouch.count", "可容纳 %1$s/%2$s 个施术核心");
        add("tc.wand.notenoughvis", "法杖中的魔力不足");
        add("tc.node.name", "灵气节点");
        add("tc.node.jar.aspect", "%1$s %2$s");
        add("tc.node.typemod", "%1$s，%2$s");
        add("nodetype.thaumaturge.normal", "标准");
        add("nodetype.thaumaturge.unstable", "震荡");
        add("nodetype.thaumaturge.dark", "凶险");
        add("nodetype.thaumaturge.tainted", "污染");
        add("nodetype.thaumaturge.hungry", "饕餮");
        add("nodetype.thaumaturge.pure", "纯净");
        add("nodemod.thaumaturge.bright", "明亮");
        add("nodemod.thaumaturge.pale", "苍白");
        add("nodemod.thaumaturge.fading", "凋零");
        add("tc.wand.noaura", "这里的灵气太微弱，无法汲取");
        add("tc.jar.noresearch", "你察觉到这种布置的潜力，但缺乏利用它的知识。");
        add("tc.jar.structure", "仪式失败——节点必须四面密封于玻璃之中，顶部加盖木质台阶屋顶。");
        add("tc.jar.vis", "仪式失败——它需要你快捷栏中法杖提供每种元始要素 %s 点魔力。");
        add("tc.dust.noresearch", "尘埃闪烁着希望之光，但你缺乏引导它的知识。");
        add("tc.workbench.staff", "长杖过于笨重，无法在工作台使用");
        add("tooltip.thaumaturge.wand.capacity", "容量 %s");
        add("tooltip.thaumaturge.wand.cost", "魔力消耗 %s%%");
        add("tooltip.thaumaturge.wand.cost.except", "魔力消耗 %1$s%%（%2$s）");
        add("item.thaumaturge.wand_cap_iron", "铁杖端");
        add("item.thaumaturge.wand_cap_copper", "铜杖端");
        add("item.thaumaturge.wand_cap_gold", "金杖端");
        add("item.thaumaturge.wand_cap_silver_inert", "惰性银杖端");
        add("item.thaumaturge.wand_cap_silver", "充能银杖端");
        add("item.thaumaturge.wand_cap_thaumium_inert", "惰性神秘杖端");
        add("item.thaumaturge.wand_cap_thaumium", "充能神秘杖端");
        add("item.thaumaturge.wand_cap_void_inert", "惰性虚空杖端");
        add("item.thaumaturge.wand_cap_void", "充能虚空杖端");
        add("item.thaumaturge.wand_rod_greatwood", "宏伟之木杖柄");
        add("item.thaumaturge.wand_rod_obsidian", "黑曜石杖柄");
        add("item.thaumaturge.wand_rod_blaze", "烈焰杖柄");
        add("item.thaumaturge.wand_rod_ice", "寒霜杖柄");
        add("item.thaumaturge.wand_rod_quartz", "石英杖柄");
        add("item.thaumaturge.wand_rod_bone", "白骨杖柄");
        add("item.thaumaturge.wand_rod_reed", "甘蔗杖柄");
        add("item.thaumaturge.wand_rod_silverwood", "银树杖柄");
        add("item.thaumaturge.staff_rod_greatwood", "宏伟之木杖芯");
        add("item.thaumaturge.staff_rod_obsidian", "黑曜石杖芯");
        add("item.thaumaturge.staff_rod_blaze", "烈焰杖芯");
        add("item.thaumaturge.staff_rod_ice", "冰霜杖芯");
        add("item.thaumaturge.staff_rod_quartz", "石英杖芯");
        add("item.thaumaturge.staff_rod_bone", "白骨杖芯");
        add("item.thaumaturge.staff_rod_reed", "甘蔗杖芯");
        add("item.thaumaturge.staff_rod_silverwood", "银树杖芯");
        add("item.thaumaturge.staff_rod_primal", "元始杖芯");
        add("item.thaumaturge.primal_charm", "元始魔力");
        add("tooltip.thaumaturge.primal_charm.0", "它好像在释放着什么.");
        add("tooltip.thaumaturge.primal_charm.1", "你恍惚听见有耳语响起.");
        add("tooltip.thaumaturge.primal_charm.2", "一阵猛烈的颠簸.");
        add("tooltip.thaumaturge.primal_charm.3", "它嗡嗡作响,声音十分舒缓.");
        add("tooltip.thaumaturge.primal_charm.4", "稍等,它刚刚闪烁着七色的光芒?");
        add("item.thaumaturge.focus_1", "空白次级核心");
        add("item.thaumaturge.focus_2", "空白高级核心");
        add("item.thaumaturge.focus_3", "空白大师核心");
        add("tooltip.thaumaturge.caster.vis_cost", "魔力消耗：每次施法 %s 魔力");
        add("tooltip.thaumaturge.focus.vis_cost", "每次施法 %s 魔力");
        add("entity.thaumaturge.aspect_orb", "要素球");
        add("entity.thaumaturge.focus_projectile", "核心投掷物");
        add("entity.thaumaturge.focus_cloud", "核心云雾");
        add("entity.thaumaturge.focus_mine", "地雷核心");
        add("entity.thaumaturge.spell_bat", "巫蝠");

        add("block.thaumaturge.node", "灵气节点");
        add("block.thaumaturge.jar_node", "缸中节点");
        add("block.thaumaturge.node_stabilizer", "节点稳定器");
        add("block.thaumaturge.node_stabilizer_advanced", "高级节点稳定器");
        add("block.thaumaturge.hole", "次元裂痕");
        add("block.thaumaturge.effect_sap", "弱化力场");

        add("focus.thaumaturge.root.name", "施法者");
        add("focus.thaumaturge.root.text", "施法者与法术的起点。");
        add("focus.thaumaturge.touch.name", "接触");
        add("focus.thaumaturge.touch.text", "使你能将效果施加到接触的物体上。");
        add("focus.thaumaturge.projectile.name", "抛射");
        add("focus.thaumaturge.projectile.text", "将核心的能量聚集成魔法球，并像投射物一样抛射出去。这种投射物的射程很远，但飞行速度缓慢且会受到重力影响。");
        add("focus.thaumaturge.bolt.name", "震击");
        add("focus.thaumaturge.bolt.text", "向目标直接发出强烈的魔法冲击，瞬间击中并产生效果，但射程有限。");
        add("focus.thaumaturge.plan.name", "规划");
        add("focus.thaumaturge.plan.text", "允许你精确规划哪些方块会受到影响。");
        add("focus.thaumaturge.cloud.name", "云雾");
        add("focus.thaumaturge.cloud.text", "制造出一个缓慢消逝，充斥魔法能量的云雾，并影响其中的所有事物。");
        add("focus.thaumaturge.mine.name", "奥术地雷");
        add("focus.thaumaturge.mine.text", "创建一个神奇的特殊结构，当敌对性生物经过时自动引爆，并释放出能量。");
        add("focus.thaumaturge.hellbat.name", "九狱");
        add("focus.thaumaturge.hellbat.text", "召唤凶猛的九狱焱蝠，用火焰与怒火袭扰目标。");
        add("focus.hellbat.bats", "蝙蝠");
        add("focus.thaumaturge.primal.name", "元始");
        add("focus.thaumaturge.primal.text", "释放出一股原始元始能量。极具破坏性，飘忽不定，偶尔还会……创造万物。");
        add("focus.thaumaturge.spellbat.name", "召唤巫蝠");
        add("focus.thaumaturge.spellbat.text", "将魔力凝聚成能追踪目标并对其施加施术核心效果的奥术蝙蝠。");

        add("focus.thaumaturge.air.name", "疾风");
        add("focus.thaumaturge.air.text", "创造一股气流冲击，击退附近的东西，但是只能造成轻微的伤害。");
        add("focus.thaumaturge.earth.name", "大地");
        add("focus.thaumaturge.earth.text", "投掷出一团泥土碎片，造成可观伤害，并可能破坏较脆弱的方块。");
        add("focus.thaumaturge.fire.name", "火焰");
        add("focus.thaumaturge.fire.text", "向目标投掷火焰并点燃它。");
        add("focus.thaumaturge.frost.name", "寒霜");
        add("focus.thaumaturge.frost.text", "向目标发射冰冷的投掷物，使用严寒的力量造成伤害。将水冻结并使生物减速。");
        add("focus.thaumaturge.break.name", "破坏");
        add("focus.thaumaturge.break.text", "召唤破坏性的能量破坏周围的几乎一切。");
        add("focus.thaumaturge.curse.name", "诅咒");
        add("focus.thaumaturge.curse.text", "召唤无序的能量来伤害并扰乱目标生物。");
        add("focus.thaumaturge.flux.name", "腐化");
        add("focus.thaumaturge.flux.text", "被施以这种效果的核心会召唤混沌而纯粹的魔力吞噬一切生命。它不会与那些死物纠缠，所以对凡俗的护甲熟视无睹。");
        add("focus.thaumaturge.rift.name", "裂隙");
        add("focus.thaumaturge.rift.text", "在墙壁或者其他表面打通异次元通道通往彼端，妈妈再也不担心你回不了家。");
        add("focus.thaumaturge.exchange.name", "更替");
        add("focus.thaumaturge.exchange.text", "将一种方块更替为另一种。");
        add("focus.thaumaturge.heal.name", "治愈");
        add("focus.thaumaturge.heal.text", "治愈普通生物，伤害亡灵类生物。");

        add("focus.thaumaturge.scatter.name", "散射");
        add("focus.thaumaturge.scatter.text", "将单个弹道分裂为多个随机数量的弹道");
        add("focus.thaumaturge.split_target.name", "分裂目标");
        add("focus.thaumaturge.split_target.text", "将一个目标分裂成两个较弱的目标。");
        add("focus.thaumaturge.split_trajectory.name", "分裂弹道");
        add("focus.thaumaturge.split_trajectory.text", "将单个弹道分裂成两个较弱的弹道。");

        add("focus.common.power", "效力");
        add("focus.common.duration", "持续时间（秒）");
        add("focus.common.radius", "半径（格）");
        add("focus.common.silk", "精准采集");
        add("focus.common.no", "否");
        add("focus.common.yes", "是");
        add("focus.common.fortune", "时运");
        add("focus.common.target", "目标类型");
        add("focus.common.friend", "友好");
        add("focus.common.enemy", "敌对");
        add("focus.common.none", "否");
        add("focus.common.options", "选项");
        add("focus.fire.burn", "燃烧时间");
        add("focus.scatter.cone", "散角（度）");
        add("focus.scatter.forks", "投射物分岔");
        add("focus.projectile.speed", "投射物速度");
        add("focus.projectile.bouncy", "弹跳");
        add("focus.projectile.seeking.friendly", "锁定友好单位");
        add("focus.projectile.seeking.hostile", "锁定敌对单位");
        add("focus.break.power", "破坏力");
        add("focus.plan.method", "摆放方式");
        add("focus.plan.full", "立体");
        add("focus.plan.surface", "平面");
        add("focus.rift.depth", "深度");
        add("focus.heal.power", "治愈");

        add("death.attack.thaumaturge.focus_fire", "%1$s被魔法烧成了焦炭");
        add("death.attack.thaumaturge.focus_fire.player", "%1$s被%2$s的魔法烧成了焦炭");
        add("block.thaumaturge.focal_manipulator", "核心镶饰台");
        add("gui.thaumaturge.wandtable.craft", "开始升级");
        add("gui.thaumaturge.wandtable.complexity", "整体复杂度");
        add("gui.thaumaturge.wandtable.xp_cost", "经验消耗");
        add("gui.thaumaturge.wandtable.vis_cost", "魔力消耗");
        add("gui.thaumaturge.wandtable.cast_cost", "每次施法消耗魔力：%s");
        add("gui.thaumaturge.wandtable.components", "需要水晶");
        add("gui.thaumaturge.wandtable.part_complexity", "复杂度：");
        add("gui.thaumaturge.wandtable.part_efficiency", "效果倍数：");
        add("gui.thaumaturge.wandtable.heal_power", "治愈");
        add("gui.thaumaturge.wandtable.problem.in_progress", "合成进行中……");
        add("gui.thaumaturge.wandtable.problem.complexity", "过于复杂：%s/%s");
        add("gui.thaumaturge.wandtable.problem.empty_nodes", "该法术还有未填充的节点");
        add("gui.thaumaturge.wandtable.problem.crystal", "缺少 %sx %s");
        add("gui.thaumaturge.wandtable.problem.no_effects", "该法术至少需要一个效果");
        add("gui.thaumaturge.wandtable.problem.xp", "需要 %s 级经验");
        add("gui.thaumaturge.wandtable.problem.ready", "可以合成了！");
    }

    private void langEnchantments() {

        add("enchantment.thaumaturge.collector", "收集");
        add("enchantment.thaumaturge.destructive", "破坏");
        add("enchantment.thaumaturge.burrowing", "奔流");
        add("enchantment.thaumaturge.sounding", "勘探");
        add("enchantment.thaumaturge.refining", "炽心");
        add("enchantment.thaumaturge.arcing", "弧光");
        add("enchantment.thaumaturge.essence", "晶化");
        add("enchantment.thaumaturge.lamplight", "灯光");
        add("block.thaumaturge.effect_glimmer", "微光");
    }

    private void langGolemancy() {

        add("item.thaumaturge.mind_clockwork", "发条之心");
        add("item.thaumaturge.mind_biothaumic", "神秘生物学之心");
        add("item.thaumaturge.module_vision", "视觉组件");
        add("item.thaumaturge.module_aggression", "攻击组件");
        add("item.thaumaturge.golem_bell", "傀儡使铃铛");
        add("item.thaumaturge.golem", "傀儡");
        add("item.thaumaturge.golem_top_hat", "傀儡饰件：礼帽");
        add("item.thaumaturge.golem_fez", "傀儡饰件：毡帽");
        add("item.thaumaturge.golem_glasses", "傀儡饰件：眼镜");
        add("item.thaumaturge.golem_bowtie", "傀儡饰件：蝴蝶结");
        add("item.thaumaturge.golem_visor", "傀儡饰件：面甲");
        add("item.thaumaturge.seal_blank", "空白印记");
        add("item.thaumaturge.seal_pickup", "操控印记：聚集");
        add("item.thaumaturge.seal_pickup_advanced", "高级操控印记：聚集");
        add("item.thaumaturge.seal_fill", "操控印记：贮存");
        add("item.thaumaturge.seal_fill_advanced", "高级操控印记：贮存");
        add("item.thaumaturge.seal_empty", "操控印记：空置");
        add("item.thaumaturge.seal_empty_advanced", "高级操控印记：空置");
        add("item.thaumaturge.seal_harvest", "操控印记：收获");
        add("item.thaumaturge.seal_butcher", "操控印记：屠宰");
        add("item.thaumaturge.seal_guard", "操控印记：守卫");
        add("item.thaumaturge.seal_guard_advanced", "高级操控印记：守卫");
        add("item.thaumaturge.seal_lumber", "操控印记：砍伐");
        add("item.thaumaturge.seal_breaker", "操控印记：采掘");
        add("item.thaumaturge.seal_breaker_advanced", "操控印记：高级采掘");
        add("item.thaumaturge.seal_use", "操控印记：使用");
        add("item.thaumaturge.seal_provider", "操控印记：供给");
        add("item.thaumaturge.seal_stock", "操控印记：仓储");
        add("block.thaumaturge.levitator", "奥术悬浮台");
        add("block.thaumaturge.potion_sprayer", "药水喷雾器");
        add("block.thaumaturge.pattern_crafter", "奥术组装器");
        add("block.thaumaturge.inlay", "红石镶边");
        add("block.thaumaturge.golem_builder", "傀儡工坊");
        add("block.thaumaturge.placeholder_iron_bars", "铁栏杆");
        add("block.thaumaturge.placeholder_cauldron", "炼药锅");
        add("block.thaumaturge.placeholder_anvil", "铁砧");
        add("block.thaumaturge.placeholder_table", "石桌");
        add("entity.thaumaturge.golem", "Thaumaturge 傀儡");
        add("entity.thaumaturge.golem_dart", "傀儡飞镖");
        add("golem.follow", "吾主，我将追随您");
        add("golem.stay", "我将在此待命，吾主");
        add("golem.rank", "等级");
        add("tc.notowned", "它并不属于你。");
        add("gui.thaumaturge.levitator", "范围设为%s格。（%s魔力/分钟）");
        trait("smart", "智慧", "这种傀儡似乎更擅长推理决断，具有智慧。");
        trait("deft", "灵巧", "这种傀儡具有高度灵活的双手，可以胜任高精度要求的工作。该属性与笨拙相互抵消。");
        trait("clumsy", "笨拙", "这种傀儡手部灵活度低，只能完成最基础的工作。该属性与灵巧相互抵消。");
        trait("fighter", "战斗", "这种傀儡具有攻击生物的能力。");
        trait("wheeled", "轮式结构", "这种傀儡采用轮式结构提高了移动速度，但是无法跳跃或走上陡坡。");
        trait("flyer", "飞行", "这种傀儡具有飞行能力，以速度为代价换取了更高的机动性。");
        trait("climber", "攀爬", "这种傀儡具有攀爬悬崖的能力。");
        trait("heavy", "笨重", "笨重的结构降低了傀儡的速度与灵活性。该属性与轻盈相互抵消。");
        trait("light", "轻盈", "轻盈的结构使得这种傀儡更加迅速与灵活。该属性与笨重相互抵消。");
        trait("fragile", "脆弱", "这种傀儡采用了精密仪器或是低强度材料，生命上限与护甲值降低。该属性与装甲相互抵消。");
        trait("repair", "自愈", "这种傀儡的自愈能力是普通傀儡的两倍。");
        trait("scout", "侦查", "这种傀儡具有优秀的视野，工作范围更大。（视野为48格，而非通常的32格）。");
        trait("armored", "装甲", "这种傀儡使用了额外材料进行加固，具有更高的护甲值。该属性与脆弱相互抵消。");
        trait("brutal", "蛮力", "这种傀儡具有更高的近战伤害。");
        trait("fireproof", "抗火", "这种傀儡不受火焰伤害。");
        trait("breaker", "破坏", "这种傀儡可以轻松地破坏大部分方块。");
        trait("hauler", "负重", "使傀儡可以携带2组物品，而不是通常的1组。");
        trait("ranged", "远程攻击", "这种傀儡具有远程攻击能力。");
        trait("blastproof", "防爆", "这种傀儡具有较高的爆炸抗性。");
        material("wood", "宏伟之木傀儡", "这个傀儡由宏伟之木制成，轻巧迅捷，但不太坚固。");
        material("iron", "铁质傀儡", "这个傀儡由铁制成，非常坚固并且能够抗火，但却十分笨重。");
        material("clay", "粘土傀儡", "这个傀儡由硬化粘土制成，这不算是坚固的材料，但使傀儡能够抗火并且更为轻巧。");
        material("brass", "黄铜傀儡", "这个傀儡由黄铜制成，它不及铁坚固，也不像铁一样耐火。虽然黄铜本身比铁重，但可以通过更好的加工方法制成更轻的框架。");
        material("thaumium", "神秘傀儡", "这个傀儡由神秘锭制成，它和铁相似，但更加坚固、耐耗。");
        material("void", "虚空傀儡", "这个傀儡由虚空金属制成，比铁稍软，使得傀儡更轻并且可以自愈。");
        head("basic", "发条头部", "默认的傀儡头部。没有特殊之处。");
        head("smart", "智慧头部", "智慧头部赋予傀儡学习以提升性能的能力，内含一个神秘生物学之心。");
        head("smart_armored", "装甲智慧头部", "使用了额外的金属板及羊毛进行加固的智慧头部。");
        head("scout", "感知头部", "使用神秘生物学之眼进行强化的傀儡头部。");
        head("smart_scout", "神秘生物学头部", "同时使用了神秘生物学之心及神秘生物学之眼进行强化的头部，此乃神秘生物学研究集大成之作。");
        arm("basic", "基础臂部", "默认的傀儡臂部，没有特殊之处。");
        arm("fine", "精巧机械臂", "末端装有高度精密的机械臂。");
        arm("claws", "钳型机械臂", "末端装有锋利而可怕的金属钳，内置攻击组件。");
        arm("breakers", "粉碎机械臂", "末端装有气动钻石砂轮。");
        arm("darts", "飞镖发射臂", "末端装有气动飞镖发射器。会自动凝结魔力形成飞镖用以发射。内置攻击组件。");
        leg("walker", "基础腿部", "默认的傀儡腿部，没有特殊之处。");
        leg("roller", "万向轮", "一个简单万向轮，速度非常快，但是无法跳跃或走上台阶。");
        leg("climber", "登山足部", "由燧石制作的攀爪强化的傀儡腿部。使傀儡可以行走在垂直的表面上。");
        leg("flyer", "悬浮组件", "改进后的奥术悬浮台赋予傀儡飞行的能力。但降低了傀儡三分之一的速度。");
        addon("none", "", "");
        addon("armored", "装甲加固", "提升傀儡护甲值。");
        addon("fighter", "攻击组件", "允许傀儡进行战斗。");
        addon("hauler", "置物架", "允许傀儡携带2组物品，而非通常的1组。");
        add("golem.prop.replant", "补种作物");
        add("golem.prop.cycle", "按白名单循环");
        add("golem.prop.meta", "匹配元数据");
        add("golem.prop.nbt", "匹配 NBT 数据");
        add("golem.prop.ore", "匹配矿物辞典");
        add("golem.prop.mod", "匹配相同模组");
        add("golem.prop.mob", "以生物为目标");
        add("golem.prop.animal", "以动物为目标");
        add("golem.prop.player", "以玩家为目标");
        add("golem.prop.left", "左键点击");
        add("golem.prop.empty", "点击空气方块");
        add("golem.prop.emptyhand", "允许空手点击");
        add("golem.prop.sneak", "模拟潜行状态点击");
        add("golem.prop.single", "仅允许提供一个物品");
        add("golem.prop.provision", "请求物品供应");
        add("golem.prop.provision.wl", "根据白名单请求物品供应");
        add("golem.prop.priority", "任务优先级");
        add("golem.prop.color", "%s傀儡");
        add("golem.prop.colorall", "所有傀儡");
        add("golem.prop.owner", "这个印记属于你");
        add("golem.prop.lock", "仅允许印记所有者持有的傀儡执行这项任务");
        add("golem.prop.unlock", "所有的傀儡都可以执行这项任务");
        add("golem.prop.redon", "受红石信号控制");
        add("golem.prop.redoff", "忽略红石信号");
        add("golem.prop.exist", "仅在目标容器可容纳物品时工作");
        add("golem.prop.leave", "总是保留一个物品在绑定容器内");
        add("golem.prop.silk", "使用精准采集");
        add("gui.thaumaturge.seal", "印记");
        add("golem.prop.blacklist", "黑名单");
        add("golem.prop.whitelist", "白名单");
        add("button.category.0", "优先级/锁定");
        add("button.category.1", "过滤器");
        add("button.category.2", "区域");
        add("button.category.3", "选项");
        add("button.category.4", "需求");
        add("button.category.0.desc", "任务优先级、傀儡颜色与印记锁定");
        add("button.category.1.desc", "印记所作用的物品");
        add("button.category.2.desc", "印记覆盖区域的大小");
        add("button.category.3.desc", "此印记的行为选项");
        add("button.category.4.desc", "傀儡必须拥有或不得拥有的特性");
        add("button.caption.x", "东西");
        add("button.caption.y", "上下");
        add("button.caption.z", "南北");
        add("button.caption.required", "需求属性");
        add("button.caption.forbidden", "受限属性");
    }

    private void langBosses() {
        add("entity.thaumaturge.cultist_leader", "血腥主教");
        add("entity.thaumaturge.cultist_leader.name.custom", "血腥主教%1$s%2$s");
        add("entity.thaumaturge.cultist_portal_greater", "巨型血腥传送门");
        add("entity.thaumaturge.eldritch_golem", "邪术魔像");
        add("entity.thaumaturge.eldritch_golem.name.custom", "邪术魔像%1$s");
        add("entity.thaumaturge.eldritch_warden", "邪术典狱官");
        add("entity.thaumaturge.eldritch_warden.name.custom", "%2$s典狱官%1$s");
        add("entity.thaumaturge.taintacle_giant", "巨型腐化触手");
        add("tc.boss.enrage", "被你那有力的攻击激怒了。");
        add("item.thaumaturge.crimson_praetor_helm", "血腥主教头盔");
        add("item.thaumaturge.crimson_praetor_chest", "血腥主教胸铠");
        add("item.thaumaturge.crimson_praetor_legs", "血腥主教护胫");
    }

    private void langConstructs() {
        add("block.thaumaturge.activator_rail", "奥术激活轨道");
        add("item.thaumaturge.turret_basic", "自动十字弩");
        add("item.thaumaturge.turret_advanced", "高级自动弩");
        add("item.thaumaturge.turret_bore", "奥术钻探机");
        add("item.thaumaturge.grapple_gun", "奥术机动装置");
        add("item.thaumaturge.grapple_gun_tip", "机动装置头部");
        add("item.thaumaturge.grapple_gun_spool", "机动装置滚轴");
        add("entity.thaumaturge.turret_crossbow", "自动十字弩");
        add("entity.thaumaturge.turret_crossbow_advanced", "高级自动弩");
        add("entity.thaumaturge.arcane_bore", "奥术钻探机");
        add("entity.thaumaturge.grapple", "钩爪");
        add("button.turretfocus.1", "以动物为目标");
        add("button.turretfocus.2", "以怪物为目标");
        add("button.turretfocus.3", "以玩家为目标");
        add("button.turretfocus.4", "以友好生物为目标");
        add("gui.thaumaturge.bore.width", "宽度：%s");
        add("gui.thaumaturge.bore.depth", "深度：%s");
        add("gui.thaumaturge.bore.speed", "速度：+%s");
        add("gui.thaumaturge.bore.properties", "其他属性：");
        add("gui.thaumaturge.bore.refining", "精炼 %s");
        add("gui.thaumaturge.bore.fortune", "时运 %s");
        add("gui.thaumaturge.bore.silktouch", "精准采集");
    }

    private void langDecorSweep() {
        add("block.thaumaturge.slab_greatwood", "宏伟之木台阶");
        add("block.thaumaturge.slab_silverwood", "银树台阶");
        add("block.thaumaturge.slab_arcane_stone", "奥术台阶");
        add("block.thaumaturge.slab_arcane_brick", "奥术石砖台阶");
        add("block.thaumaturge.slab_ancient", "荒古台阶");
        add("block.thaumaturge.slab_eldritch", "邪术石头台阶");
        add("block.thaumaturge.stairs_greatwood", "宏伟之木楼梯");
        add("block.thaumaturge.stairs_silverwood", "银树楼梯");
        add("block.thaumaturge.table_wood", "木桌");
        add("block.thaumaturge.table_stone", "石桌");
        add("block.thaumaturge.paving_stone_travel", "旅行者铺路石");
        add("block.thaumaturge.paving_stone_barrier", "屏障石");
        add("block.thaumaturge.barrier", "屏障");
        add("block.thaumaturge.amber_brick", "琥珀砖块");
        add("block.thaumaturge.flesh_block", "血肉方块");
        add("block.thaumaturge.effect_shock", "静电力场");
    }

    private void langOuterLands() {
        add("block.thaumaturge.obsidian_tile", "黑曜石瓦块");
        add("block.thaumaturge.obsidian_totem", "黑曜石图腾");
        add("block.thaumaturge.obsidian_totem_charged", "蕴灵黑曜石图腾");
        add("block.thaumaturge.eldritch_stone", "邪术石");
        add("block.thaumaturge.eldritch_stone_inert", "惰性邪术石");
        add("block.thaumaturge.eldritch_rock", "邪术岩");
        add("block.thaumaturge.eldritch_crust", "邪术结壳");
        add("block.thaumaturge.eldritch_crust_glowing", "发光邪术结壳");
        add("block.thaumaturge.stairs_eldritch", "邪术石楼梯");
        add("block.thaumaturge.eldritch_door", "发光邪术石");
        add("block.thaumaturge.eldritch_pedestal", "邪术基座");
        add("block.thaumaturge.eldritch_stone_crystal", "晶化邪术石");
        add("block.thaumaturge.eldritch_nothing", "虚无");
        add("block.thaumaturge.eldritch_lock", "邪术锁");
        add("block.thaumaturge.eldritch_crab_spawner", "荒古锁孔");
        add("block.thaumaturge.eldritch_trap", "邪术石");
        add("block.thaumaturge.eldritch_altar", "邪术祭坛");
        add("block.thaumaturge.eldritch_obelisk", "邪术方尖碑");
        add("block.thaumaturge.eldritch_pillar", "邪术柱");
        add("block.thaumaturge.eldritch_capstone", "邪术拱顶石");
        add("block.thaumaturge.eldritch_portal", "邪术传送门");
        add("item.thaumaturge.eldritch_eye", "邪术之眼");
        add("item.thaumaturge.runed_tablet", "符文石板");
        add("tc.boss.warden", "远处的基座涌动着黑色的能量，有什么东西要出来了……");
        add("tc.boss.golem", "守卫孤独地伫立在这房间中，但能量已从它那古老的躯壳上溢出，你意识到它不久就将恢复活动。");
        add("tc.boss.crimson", "那猩红传送门的开启预示着血腥邪徒的到来，你必须尽快关闭它！");
        add("tc.boss.taint", "房间那头传来组织生长发出的悉悉索索声，不太对劲的声音……有什么正在滋生……");
        add("gui.thaumaturge.altar.not_enough_vis", "当地灵气过于微弱，无法撕裂帷幕。至少需要100点魔力。");
    }

    private void langEldritch() {

        add("entity.thaumaturge.pech", "岩精强盗");
        add("entity.thaumaturge.pech.mage", "岩精法师");
        add("entity.thaumaturge.pech.stalker", "岩精猎手");
        add("entity.thaumaturge.eldritch_crab", "邪术爪牙");
        add("entity.thaumaturge.inhabited_zombie", "行尸走肉");
        add("entity.thaumaturge.eldritch_guardian", "邪术守卫");
        add("entity.thaumaturge.cultist_knight", "血腥骑士");
        add("entity.thaumaturge.cultist_cleric", "血腥教徒");
        add("entity.thaumaturge.cultist_portal_lesser", "小型血腥传送门");
        add("entity.thaumaturge.eldritch_orb", "邪术球");
        add("entity.thaumaturge.golem_orb", "奥术球");
        add("item.thaumaturge.pech_spawn_egg", "岩精刷怪蛋");
        add("item.thaumaturge.eldritch_crab_spawn_egg", "邪术爪牙刷怪蛋");
        add("item.thaumaturge.inhabited_zombie_spawn_egg", "行尸走肉刷怪蛋");
        add("item.thaumaturge.eldritch_guardian_spawn_egg", "邪术守卫刷怪蛋");
        add("item.thaumaturge.cultist_knight_spawn_egg", "血腥骑士刷怪蛋");
        add("item.thaumaturge.cultist_cleric_spawn_egg", "血腥教徒刷怪蛋");
        add("item.thaumaturge.cultist_portal_lesser_spawn_egg", "小型血腥传送门刷怪蛋");
        add("item.thaumaturge.cultist_leader_spawn_egg", "血腥主教刷怪蛋");
        add("item.thaumaturge.cultist_portal_greater_spawn_egg", "巨型血腥传送门刷怪蛋");
        add("item.thaumaturge.eldritch_warden_spawn_egg", "邪术典狱官刷怪蛋");
        add("item.thaumaturge.eldritch_golem_spawn_egg", "邪术魔像刷怪蛋");
        add("item.thaumaturge.taintacle_giant_spawn_egg", "巨型腐化触手刷怪蛋");
        add("item.thaumaturge.pech_wand", "岩精法杖");
        add("item.thaumaturge.crimson_blade", "血腥之刃");
        add("item.thaumaturge.crimson_boots", "血腥邪徒靴子");
        add("item.thaumaturge.crimson_robe_helm", "血腥邪徒兜帽");
        add("item.thaumaturge.crimson_robe_chest", "血腥邪徒法袍");
        add("item.thaumaturge.crimson_robe_legs", "血腥邪徒护腿");
        add("item.thaumaturge.crimson_plate_helm", "血腥邪徒头盔");
        add("item.thaumaturge.crimson_plate_chest", "血腥邪徒胸甲");
        add("item.thaumaturge.crimson_plate_legs", "血腥邪徒护胫");
        add("enchantment.special.sapgreat", "高级削弱");
        add("item.curio.text", "它会揭示什么样的知识呢？");
        add("not.pechwand", "你暂时还无法理解它的作用。");
        add("got.pechwand", "嗯……这看上去非常有趣，我应该进行一些深入研究。");
        add("item.thaumaturge.loot_bag_common", "寻常藏宝袋");
        add("item.thaumaturge.loot_bag_uncommon", "稀有藏宝袋");
        add("item.thaumaturge.loot_bag_rare", "罕见藏宝袋");
        add("tc.lootbag", "右击以开启，或存留以备交易");
        add("champion.thaumaturge.name", "%1$s %2$s");
        add("champion.mod.bold", "悍勇无畏的");
        add("champion.mod.spine", "张牙舞爪的");
        add("champion.mod.armor", "全副武装的");
        add("champion.mod.mighty", "威猛强力的");
        add("champion.mod.grim", "冷酷无情的");
        add("champion.mod.warded", "百毒不侵的");
        add("champion.mod.warp", "乖戾扭曲的");
        add("champion.mod.undying", "永恒不朽的");
        add("champion.mod.fiery", "火爆狂躁的");
        add("champion.mod.sickly", "疾病缠身的");
        add("champion.mod.venomous", "荼毒四野的");
        add("champion.mod.vampiric", "嗜血成性的");
        add("champion.mod.infested", "瘟疫横行的");
        add("champion.mod.tainted", "污染");
        add("attributes.thaumaturge.champion_mod", "精英加成");
        add("attributes.thaumaturge.tainted_mod", "腐化加成");
        add("block.thaumaturge.loot_crate_common", "寻常箱");
        add("block.thaumaturge.loot_crate_uncommon", "稀有箱");
        add("block.thaumaturge.loot_crate_rare", "罕见箱");
        add("block.thaumaturge.loot_urn_common", "寻常瓮");
        add("block.thaumaturge.loot_urn_uncommon", "稀有瓮");
        add("block.thaumaturge.loot_urn_rare", "罕见瓮");
    }

    private void trait(String key, String name, String text) {
        Identifier id = TCIds.rl(key);
        add(GolemTrait.nameKey(id), name);
        add(GolemTrait.descriptionKey(id), text);
    }

    private void material(String key, String name, String text) {
        Identifier id = TCIds.rl(key);
        add(GolemMaterial.nameKey(id), name);
        add(GolemMaterial.descriptionKey(id), text);
    }

    private void partLang(String kind, String key, String name, String text) {
        Identifier id = TCIds.rl(key);
        add(GolemPart.nameKey(kind, id), name);
        add(GolemPart.descriptionKey(kind, id), text);
    }

    private void head(String key, String name, String text) {
        partLang("head", key, name, text);
    }

    private void arm(String key, String name, String text) {
        partLang("arm", key, name, text);
    }

    private void leg(String key, String name, String text) {
        partLang("leg", key, name, text);
    }

    private void addon(String key, String name, String text) {
        partLang("addon", key, name, text);
    }

    private void addJade() {
        add("config.jade.plugin_thaumaturge.node", "灵气节点信息");
        add("config.jade.plugin_thaumaturge.essentia", "源质内容");
        add("config.jade.plugin_thaumaturge.machine", "机器进度");
        add("config.jade.plugin_thaumaturge.golem", "傀儡信息");
        add("jade.thaumaturge.aspect_amount", "%s x%s");
        add("jade.thaumaturge.aspect_separator", "，");
        add("jade.thaumaturge.node.type.normal", "灵气节点");
        add("jade.thaumaturge.node.type.unstable", "震荡节点");
        add("jade.thaumaturge.node.type.dark", "凶险节点");
        add("jade.thaumaturge.node.type.tainted", "腐化节点");
        add("jade.thaumaturge.node.type.pure", "纯净节点");
        add("jade.thaumaturge.node.type.hungry", "饕餮节点");
        add("jade.thaumaturge.node.modifier.bright", "明亮");
        add("jade.thaumaturge.node.modifier.pale", "苍白");
        add("jade.thaumaturge.node.modifier.fading", "凋零");
        add("jade.thaumaturge.node.modified", "%s %s");
        add("jade.thaumaturge.node.aspects", "要素：%s");
        add("jade.thaumaturge.node.energized", "充能");
        add("jade.thaumaturge.node.feeds_aura", "正在从当地灵气中凝聚原始魔力");
        add("jade.thaumaturge.node.feeds_flux", "正在吞噬当地灵气中的咒波");
        add("jade.thaumaturge.node.reverts_to", "恢复为：%s");
        add("jade.thaumaturge.essentia.empty", "空置");
        add("jade.thaumaturge.essentia.fill", "%s：%s / %s");
        add("jade.thaumaturge.essentia.contents", "源质：%s");
        add("jade.thaumaturge.machine.progress", "进度：%s%%");
        add("jade.thaumaturge.machine.heat", "热量：%s%%");
        add("jade.thaumaturge.transducer.status.0", "下方没有节点");
        add("jade.thaumaturge.transducer.status.1", "转换中");
        add("jade.thaumaturge.transducer.status.2", "节点已充能");
        add("jade.thaumaturge.transducer.charge", "充能：%s%%");
        add("jade.thaumaturge.relay.linked_node", "已连接至充能节点");
        add("jade.thaumaturge.relay.linked_relay", "经由 %s 个中继器连接");
        add("jade.thaumaturge.relay.unlinked", "范围内没有充能节点");
        add("jade.thaumaturge.golem.rank", "等级 %s（%s 经验）");
    }
}
