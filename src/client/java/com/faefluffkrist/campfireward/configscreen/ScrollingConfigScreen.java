package com.faefluffkrist.campfireward.configscreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
/** Pixel viewport for wrapped descriptions and rows of different heights. */
abstract class ScrollingConfigScreen extends Screen {
 @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
  Screen.extractMenuBackgroundTexture(g,net.minecraft.resources.Identifier.withDefaultNamespace("textures/block/cobbled_deepslate.png"),0,0,0,0,width,height);
  g.fill(0,0,width,height,0x99000000);
 }
 protected int scrollPixels,listTop,listBottom,listX,listWidth;
 private int contentHeight;
 private boolean dragging;
 ScrollingConfigScreen(Component title){super(title);}
 protected void layoutContent(int total,int top,int bottom,int maximumWidth){
  contentHeight=total;listTop=top;listBottom=Math.max(top+1,bottom);
  listWidth=Math.min(maximumWidth,width-48);listX=width/2-listWidth/2;
  scrollPixels=Math.max(0,Math.min(scrollPixels,maxScroll()));
 }
 private int maxScroll(){return Math.max(0,contentHeight-(listBottom-listTop));}
 protected boolean controlVisible(int y,int h){return y>=listTop&&y+h<=listBottom;}
 protected void drawScrollbar(GuiGraphicsExtractor g){
  int max=maxScroll();if(max==0)return;
  int track=listBottom-listTop,thumb=Math.min(track,Math.max(16,track*track/contentHeight));
  int x=listX+listWidth+7,top=listTop+(track-thumb)*scrollPixels/max;
  g.fill(x,listTop,x+5,listBottom,0x663D454B);
  g.fill(x,top,x+5,top+thumb,dragging?0xFFFFCF77:0xFFB7C7CC);
 }
 private void setScroll(double mouseY){
  int max=maxScroll();if(max==0)return;
  int track=listBottom-listTop,thumb=Math.min(track,Math.max(16,track*track/contentHeight));
  int next=Math.max(0,Math.min(max,(int)Math.round((mouseY-listTop-thumb/2.0)/Math.max(1,track-thumb)*max)));
  if(next!=scrollPixels){scrollPixels=next;rebuildWidgets();}
 }
 @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
  if(my>=listTop&&my<=listBottom&&vertical!=0&&maxScroll()>0){
   int next=Math.max(0,Math.min(maxScroll(),scrollPixels+(vertical>0?-24:24)));
   if(next!=scrollPixels){scrollPixels=next;rebuildWidgets();}return true;
  }return super.mouseScrolled(mx,my,horizontal,vertical);
 }
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){
  int x=listX+listWidth+7;
  if(e.button()==0&&maxScroll()>0&&e.x()>=x-3&&e.x()<=x+8&&e.y()>=listTop&&e.y()<=listBottom){dragging=true;setScroll(e.y());return true;}
  return super.mouseClicked(e,doubleClick);
 }
 @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging){setScroll(e.y());return true;}return super.mouseDragged(e,dx,dy);}
 @Override public boolean mouseReleased(MouseButtonEvent e){if(dragging){dragging=false;return true;}return super.mouseReleased(e);}
}
