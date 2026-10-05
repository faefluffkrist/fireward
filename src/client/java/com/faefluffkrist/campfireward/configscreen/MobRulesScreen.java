package com.faefluffkrist.campfireward.configscreen;

import com.faefluffkrist.campfireward.FirewardConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import java.util.*;

public final class MobRulesScreen extends ScrollingConfigScreen {
    private final FirewardScreen parent;
    private final FirewardConfig draft;
    private final String namespace;
    private String query="";
    private List<Entry> entries=List.of();
    private record Entry(String id,String name) {}
    private static final String[] HEADERS={"R Fear","S Fear","R Aura","S Aura","R Seek","S Seek","Watch"};
    public MobRulesScreen(FirewardScreen parent,String namespace) {
        super(Component.literal("Fireward Mob Rules"));this.parent=parent;this.namespace=namespace;
        draft=FirewardConfig.copy(parent.draft);
    }
    private List<Map<String,Boolean>> maps(FirewardConfig c) {
        return List.of(c.regular.fearMobs,c.soul.fearMobs,c.regular.auraMobs,c.soul.auraMobs,
            c.regular.attractionMobs,c.soul.attractionMobs,c.watchingMobs);
    }
    @Override protected void init() {
        int w=Math.min(560,width-48),x=(width-w)/2;
        EditBox search=new EditBox(font,x,34,w,20,Component.literal("Search mobs"));
        search.setMaxLength(128);search.setValue(query);
        search.setResponder(value->{query=value;scrollPixels=0;rebuildWidgets();});addRenderableWidget(search);setInitialFocus(search);
        List<Entry> found=new ArrayList<>();
        for(var type:BuiltInRegistries.ENTITY_TYPE) {
            String id=BuiltInRegistries.ENTITY_TYPE.getKey(type).toString(),name=type.getDescription().getString();
            if(!id.startsWith(namespace) || !(id+" "+name).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;
            boolean mob=type.getCategory()!=MobCategory.MISC || id.equals("minecraft:iron_golem") || id.equals("minecraft:snow_golem");
            if(minecraft.level!=null)try {mob=type.create(minecraft.level,EntitySpawnReason.LOAD) instanceof Mob;}catch(RuntimeException ignored){}
            if(mob)found.add(new Entry(id,name));
        }
        found.sort(Comparator.comparing(Entry::id));entries=found;
        layoutContent(entries.size()*42,100,height-66,560);
        int bw=Math.min(38,(listWidth-150)/7),start=listX+listWidth-7*bw,y=listTop-scrollPixels;
        for(Entry entry:entries) {
            if(controlVisible(y+10,20))for(int i=0;i<7;i++) {
                var map=maps(draft).get(i);Boolean state=map.get(entry.id);
                Button button=addRenderableWidget(Button.builder(Component.literal(state==null?"AUTO":state?"ON":"OFF")
                    .withStyle(state==null?ChatFormatting.YELLOW:state?ChatFormatting.GREEN:ChatFormatting.RED),b->{
                        if(!map.containsKey(entry.id))map.put(entry.id,true);
                        else if(map.get(entry.id))map.put(entry.id,false);else map.remove(entry.id);
                        rebuildWidgets();
                    }).bounds(start+i*bw,y+10,bw-2,20).build());
                button.active=!parent.remote();
                button.setTooltip(Tooltip.create(Component.literal(HEADERS[i]+": AUTO follows Fireward's default classification. ON includes; OFF excludes.")));
            }
            y+=42;
        }
        int left=width/2-156;
        Button defaults=addRenderableWidget(Button.builder(Component.literal("Defaults"),b->{
            for(var map:maps(draft))map.keySet().removeIf(id->id.startsWith(namespace));rebuildWidgets();
        }).bounds(left,height-28,100,20).build());defaults.active=!parent.remote();
        Button apply=addRenderableWidget(Button.builder(Component.literal("Apply"),b->{
            var target=maps(parent.draft);var source=maps(draft);
            for(int i=0;i<target.size();i++){target.get(i).clear();target.get(i).putAll(source.get(i));}onClose();
        }).bounds(left+106,height-28,100,20).build());apply.active=!parent.remote();
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(left+212,height-28,100,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        super.extractRenderState(g,mx,my,delta);
        g.centeredText(font,title,width/2,12,0xFFFFFFFF);
        g.centeredText(font,Component.literal("R = regular • S = soul • Seek = attraction • Watch = placement restriction"),width/2,64,0xFFB7C7CC);
        int bw=Math.min(38,(listWidth-150)/7),start=listX+listWidth-7*bw;
        for(int i=0;i<7;i++)g.centeredText(font,Component.literal(HEADERS[i]),start+i*bw+bw/2,86,0xFFFFE0A1);
        g.enableScissor(listX,listTop,listX+listWidth,listBottom);
        int y=listTop-scrollPixels;
        for(Entry entry:entries){
            g.fill(listX,y,start-4,y+36,0x40333C42);
            String name=font.plainSubstrByWidth(entry.name,start-listX-12);
            String id=font.plainSubstrByWidth(entry.id,start-listX-12);
            g.text(font,Component.literal(name),listX+6,y+8,0xFFFFE0A1);
            g.text(font,Component.literal(id),listX+6,y+21,0xFFB7C7CC);y+=42;
        }
        g.disableScissor();drawScrollbar(g);
        g.centeredText(font,Component.literal(parent.remote()?"Server settings are read-only":"Apply, then Save on the main screen. Back discards this page."),width/2,height-48,0xFFB7C7CC);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
