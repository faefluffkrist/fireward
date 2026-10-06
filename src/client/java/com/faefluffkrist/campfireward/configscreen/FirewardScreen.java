package com.faefluffkrist.campfireward.configscreen;

import com.faefluffkrist.campfireward.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.lang.reflect.*;
import java.util.*;

public final class FirewardScreen extends ScrollingConfigScreen {
    private static final String[] TABS = {"General","Regular Campfire","Soul Campfire","Mob Rules"};
    private final Screen parent;
    final FirewardConfig draft;
    private int category;
    private String status = "";
    private List<Row> rows = List.of();
    private final Map<String,String> pending = new HashMap<>();
    private final Set<String> invalid = new HashSet<>();
    private record Row(Object owner, Field field, ConfigOption option, String prefix) {
        String name() { return prefix + option.label(); }
        String key() { return prefix + field.getName(); }
        Object get() { try { return field.get(owner); } catch (Exception e) { throw new IllegalStateException(e); } }
        void set(Object value) { try { field.set(owner,value); } catch (Exception e) { throw new IllegalStateException(e); } }
    }
    public FirewardScreen(Screen parent) {
        super(Component.literal("Fireward Configuration"));
        this.parent=parent;
        draft=FirewardConfig.copy(remote() && FirewardConfig.serverOverride != null ? FirewardConfig.serverOverride : FirewardConfig.editable);
    }
    boolean remote() { return net.minecraft.client.Minecraft.getInstance().getConnection()!=null
        && !net.minecraft.client.Minecraft.getInstance().hasSingleplayerServer(); }
    private static void fields(List<Row> rows,Object owner,String prefix) {
        for(Field field:owner.getClass().getFields()) {
            ConfigOption option=field.getAnnotation(ConfigOption.class);
            if(option!=null) rows.add(new Row(owner,field,option,prefix));
        }
    }
    private List<Row> options() {
        List<Row> result=new ArrayList<>();
        if(category==0) fields(result,draft,"");
        else {
            var fire=draft.fire(category==2);
            fields(result,fire,category==2?"Soul • ":"Regular • ");
            fields(result,fire.slowness,(category==2?"Soul":"Regular")+" Effect 1 • ");
            fields(result,fire.weakness,(category==2?"Soul":"Regular")+" Effect 2 • ");
            fields(result,fire.glowing,(category==2?"Soul":"Regular")+" Effect 3 • ");
        }
        return result;
    }
    private List<String> wrap(String text,int availableWidth) {
        List<String> result=new ArrayList<>(); String line="";
        for(String word:text.split("\\s+")) {
            String next=line.isEmpty()?word:line+" "+word;
            if(!line.isEmpty() && font.width(next)>availableWidth) { result.add(line);line=word; }
            else line=next;
        }
        if(!line.isEmpty())result.add(line);
        return result;
    }
    private boolean available(Row row) {
        String name=row.field.getName();
        return (!name.equals("friendsAndFoes") || FabricLoader.getInstance().isModLoaded("friendsandfoes"))
            && (!name.equals("takesAPillage") || FabricLoader.getInstance().isModLoaded("takesapillage"));
    }
    private String description(Row row) {
        String text=row.option.description()+(available(row)?"":" Mod not installed.");
        return row.field.getType()==int.class || row.field.getType()==double.class ? text+" Range: "+row.option.min()+"–"+row.option.max()+"." : text;
    }
    private int rowHeight(Row row) {
        int textWidth=listWidth-126;
        int result=24+10*wrap(row.name(),textWidth).size()+10*wrap(description(row),textWidth).size();
        // Long strings get a full-width edit field beneath the description.
        return row.field.getType()==String.class ? result+24 : result;
    }
    @Override protected void init() {
        int widthAvailable=Math.min(560,width-48),x=(width-widthAvailable)/2;
        int tab=(widthAvailable-12)/4;
        for(int i=0;i<TABS.length;i++) {
            final int selected=i;
            Button b=addRenderableWidget(Button.builder(Component.literal(TABS[i]),button -> {
                if(selected==3) { minecraft.gui.setScreen(new MobRulesScreen(this,""));return; }
                category=selected;scrollPixels=0;status="";rebuildWidgets();
            }).bounds(x+i*(tab+4),34,tab,20).build());
            b.active=category!=i;
        }
        compatibility("Friends & Foes","friendsandfoes",x,height-88,(widthAvailable-4)/2);
        compatibility("It Takes a Pillage","takesapillage",x+(widthAvailable+4)/2,height-88,(widthAvailable-4)/2);
        rows=options();
        listWidth=widthAvailable;
        layoutContent(rows.stream().mapToInt(this::rowHeight).sum(),82,height-102,560);
        int y=listTop-scrollPixels;
        for(Row row:rows) {
            int h=rowHeight(row),by=y+Math.max(6,(h-30)/2);
            Class<?> type=row.field.getType();
            if(type==boolean.class && controlVisible(by,20)) {
                boolean on=(Boolean)row.get();
                Button button=addRenderableWidget(Button.builder(Component.literal(on?"ON":"OFF")
                    .withStyle((remote() || !available(row))?ChatFormatting.GRAY:on?ChatFormatting.GREEN:ChatFormatting.RED),b -> {
                        row.set(!(Boolean)row.get());status="";rebuildWidgets();
                    }).bounds(listX+listWidth-72,by,72,20).build());button.active=!remote() && available(row);
            } else if(type!=boolean.class) {
                int ey=type==String.class?y+h-30:by;
                int ex=type==String.class?listX+6:listX+listWidth-104;
                int ew=type==String.class?listWidth-12:104;
                if(controlVisible(ey,20)) {
                    EditBox box=new EditBox(font,ex,ey,ew,20,Component.literal(row.name()));
                    box.setMaxLength(row.field.getName().equals("warningText")?256:128);
                    box.setValue(pending.getOrDefault(row.key(),String.valueOf(row.get())));
                    box.setEditable(!remote());
                    box.setTextColor(invalid.contains(row.key())?0xFFFF5555:0xFFE0E0E0);
                    box.setResponder(value -> update(row,value,box));
                    addRenderableWidget(box);
                }
            }
            y+=h;
        }
        int bw=Math.min(100,(width-56)/3),left=width/2-(3*bw+12)/2;
        Button defaults=addRenderableWidget(Button.builder(Component.literal("Defaults"),b -> defaults())
            .bounds(left,height-28,bw,20).build());defaults.active=!remote();
        Button save=addRenderableWidget(Button.builder(Component.literal("Save"),b -> save())
            .bounds(left+bw+6,height-28,bw,20).build());save.active=!remote();
        addRenderableWidget(Button.builder(Component.literal("Back"),b -> onClose()).bounds(left+2*(bw+6),height-28,bw,20).build());
    }
    private void update(Row row,String value,EditBox box) {
        pending.put(row.key(),value);
        try {
            Class<?> type=row.field.getType();Object parsed=value;
            if(type==int.class) parsed=Integer.parseInt(value);
            if(type==double.class) parsed=Double.parseDouble(value);
            if(parsed instanceof Number number && (!Double.isFinite(number.doubleValue())
                    || number.doubleValue()<row.option.min() || number.doubleValue()>row.option.max()))
                throw new IllegalArgumentException("Out of range");
            if(row.field.getName().equals("effectId") && !FirewardConfig.validEffect(value))
                throw new IllegalArgumentException("Unknown effect");
            if(row.field.getName().equals("warningColor") && (FirewardConfig.color(value)==null
                    || FirewardConfig.color(value).ordinal()>ChatFormatting.WHITE.ordinal())) throw new IllegalArgumentException("Unknown color");
            row.set(parsed);invalid.remove(row.key());box.setTextColor(0xFFE0E0E0);
            status="";
        } catch(RuntimeException e) {invalid.add(row.key());box.setTextColor(0xFFFF5555);status="Fix red fields before saving";}
    }
    private void compatibility(String label,String id,int x,int y,int w) {
        boolean loaded=FabricLoader.getInstance().isModLoaded(id);
        Button button=addRenderableWidget(Button.builder(Component.literal(label).withStyle(loaded?ChatFormatting.WHITE:ChatFormatting.GRAY),
            b -> minecraft.gui.setScreen(new MobRulesScreen(this,id+":"))).bounds(x,y,w,20).build());
        button.active=loaded;
        button.setTooltip(Tooltip.create(Component.literal(loaded?"Open "+label+" mob rules":label+" is not loaded")));
    }
    private void defaults() {
        FirewardConfig defaults=new FirewardConfig();
        if(category==0) {
            for(Field field:FirewardConfig.class.getFields()) if(field.getAnnotation(ConfigOption.class)!=null)
                try {field.set(draft,field.get(defaults));}catch(Exception e){throw new IllegalStateException(e);}
        } else if(category==1) draft.regular=defaults.regular; else draft.soul=defaults.soul;
        pending.clear();invalid.clear();status="Defaults restored for this page";rebuildWidgets();
    }
    private void save() {
        if(remote())return;
        if(!invalid.isEmpty()) {status="Fix red fields before saving";return;}
        if(FirewardConfig.same(draft,FirewardConfig.editable)) {minecraft.gui.setScreen(parent);return;}
        FirewardConfig previous=FirewardConfig.editable;
        FirewardConfig.editable=FirewardConfig.copy(draft);
        try {FirewardConfig.save();minecraft.gui.setScreen(new RelogNoticeScreen(parent));}
        catch(RuntimeException failure) {FirewardConfig.editable=previous;status="Could not save; changes are still pending";}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mx,int my,float delta) {
        super.extractRenderState(graphics,mx,my,delta);
        graphics.centeredText(font,title,width/2,12,0xFFFFFFFF);
        graphics.centeredText(font,Component.literal(TABS[category]+" settings"),width/2,64,0xFFFFE0A1);
        graphics.enableScissor(listX,listTop,listX+listWidth,listBottom);
        int y=listTop-scrollPixels;
        for(Row row:rows) {
            int h=rowHeight(row);
            if(y+h>=listTop && y<listBottom) {
                graphics.fill(listX,y,listX+listWidth-(row.field.getType()==String.class?0:114),y+h-8,0x40333C42);
                int ty=y+6;
                for(String line:wrap(row.name(),listWidth-126)) {graphics.text(font,Component.literal(line),listX+6,ty,remote()?0xFF888888:0xFFFFE0A1);ty+=10;}
                ty+=3;
                String description=description(row);
                for(String line:wrap(description,listWidth-126)) {graphics.text(font,Component.literal(line),listX+6,ty,remote()?0xFF888888:0xFFB7C7CC);ty+=10;}
            }
            y+=h;
        }
        graphics.disableScissor();drawScrollbar(graphics);
        String footer=remote()?"Server controls gameplay; edit its fireward.json":status.isEmpty()?"Save changes, then reopen your world":status;
        graphics.centeredText(font,Component.literal(footer),width/2,height-54,0xFFB7C7CC);
        graphics.centeredText(font,Component.literal("Back discards unsaved changes"),width/2,height-42,0xFF888888);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
