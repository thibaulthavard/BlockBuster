package BlockBuster;
 
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
 
/**
 * Example 12 - how to give objects physical properties so they bounce and fall.
 * @author base code by double1984, updated by zathras
 */
public class BlockBuster extends SimpleApplication 
implements ActionListener,PhysicsCollisionListener, AnimEventListener {
 
  public static void main(String args[]) {
    BlockBuster app = new BlockBuster();
    app.start();
  }
 
  /** Prepare the Physics Application State (jBullet) */
  private BulletAppState bulletAppState;
 
  /** Prepare Materials */
  Material wall_mat;
  Material stone_mat;
  Material floor_mat;
 
  /** Prepare geometries and physical nodes for bricks and cannon balls. */
  private RigidBodyControl    brick_phy;
  private RigidBodyControl    block_phy;
  private static final Box    box;
  
//  private RigidBodyControl brick_ens;
//  private static final Sphere sphere_brick;
  
  private RigidBodyControl    ball_phy;
  private static final Sphere sphere;
  private RigidBodyControl    floor_phy;
  private static final Box    floor;
  private RigidBodyControl    block_floor_phy;
 
  /** dimensions used for bricks and wall */
  private static final float brickLength = 0.1f;
  private static final float brickWidth  = 0.1f;
  private static final float brickHeight = 0.1f;
  
   private Node gameLevel;
   private PhysicsCharacter player;
   private Vector3f walkDirection = new Vector3f();
   private static boolean useHttp = false;
   private boolean left=false,right=false,up=false,down=false;
   
   Geometry floor_geo;
   private Node node_floor ;
   private Node obj_pierre = new Node("node_bricks");
   private int compteur;
   //private Geometry[][][] geom = new Geometry[5][5][5];
   private Vector3f dir;
  /** Teste node objet **/
  //Node obj_pierre;
  
   /*Pioche*/
   private Spatial pioche;
   private AnimChannel channel;
   private AnimControl control;
   private CameraNode camNode;
    
  static {
    /** Initialize the cannon ball geometry */
    sphere = new Sphere(32, 32, 0.4f, true, false);
    sphere.setTextureMode(TextureMode.Projected);
    /** Initialize the brick geometry */
    box = new Box(Vector3f.ZERO, brickLength, brickHeight, brickWidth);
    box.scaleTextureCoordinates(new Vector2f(0.5f, 0.5f));
    /** Initialize the floor geometry */
    floor = new Box(Vector3f.ZERO, 100f, 0.1f, 100f);
    floor.scaleTextureCoordinates(new Vector2f(100, 100));
  }
  
  
 
  @Override
  public void simpleInitApp() {
    bulletAppState = new BulletAppState();

    bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
    stateManager.attach(bulletAppState);
    
    
    /** Set up Physics Game */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    bulletAppState.getPhysicsSpace().addCollisionListener(this);
    bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    compteur = 0;
    /** Configure cam to look at scene */
    //cam.setLocation(new Vector3f(0, 4f, 6f));
    //cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
    /** Initialize the scene, materials, and physics space */
    initMaterials();

    //initWall(new Vector3f(0,10,0));
    obj_pierre.move(10,0,0);
    initWall(new Vector3f(10,0,0));
    initFloor();
    initCrossHairs();

    setupKeys();
  
     player = new PhysicsCharacter(new SphereCollisionShape(5), .1f);
     player.setJumpSpeed(20);
     player.setFallSpeed(30);
     player.setGravity(20);

     player.setPhysicsLocation(new Vector3f(0, 10, 10));

//     rootNode.attachChild(gameLevel);
     
     pioche = assetManager.loadModel("Models/pioche.j3o");
     pioche.scale(0.3f, 0.3f, 0.3f);
     rootNode.attachChild(pioche);
     
     control = rootNode.getChild("Pioche-ogremesh").getControl(AnimControl.class);
     control.addListener(this);
     channel = control.createChannel();
                 
     bulletAppState.getPhysicsSpace().add(player);
      
  }

 
  /** Initialize the materials used in this scene. */
  public void initMaterials() {
    wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
    key.setGenerateMips(true);
    Texture tex = assetManager.loadTexture(key);
    wall_mat.setTexture("ColorMap", tex);
 
    stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
    key2.setGenerateMips(true);
    Texture tex2 = assetManager.loadTexture(key2);
    stone_mat.setTexture("ColorMap", tex2);
 
    floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key3 = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
    key3.setGenerateMips(true);
    Texture tex3 = assetManager.loadTexture(key3);
    tex3.setWrap(WrapMode.Repeat);
    floor_mat.setTexture("ColorMap", tex3);
  }
 
  /** Make a solid floor and add it to the scene. */
  public void initFloor() {
    node_floor = new Node("floor");
    floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setLocalTranslation(0, -0.1f, 0);
    node_floor.attachChild(floor_geo);
    rootNode.attachChild(node_floor);
    /* Make the floor physical with mass 0.0f! */
    floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    bulletAppState.getPhysicsSpace().add(floor_phy);
    //floor_phy.setFriction(1.0f);
  }
 
  /** This loop builds a wall out of individual bricks. */
  public void initWall(Vector3f loc) {
    Node node_bloc = new Node("brick" + compteur);
    Geometry[][][] geom = new Geometry[5][5][5];
    for(int k = 0; k <5; k++){
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                Vector3f vt =
                 new Vector3f((i * brickLength*2f)+loc.x, (j*brickHeight*2f)+loc.y, (k*brickWidth*2f)+loc.z);
                //if(mat[k][i][j]){
                //System.out.println("vt = "+vt.x+" "+vt.y+" "+vt.z);

                geom[k][i][j] = new Geometry("brick", box);
                geom[k][i][j].setMaterial(wall_mat);
                geom[k][i][j].setLocalTranslation(vt);
                
                
                brick_phy = new RigidBodyControl(0.5f);
                geom[k][i][j].addControl(brick_phy);
                bulletAppState.getPhysicsSpace().add(brick_phy);
                
               
                if(i>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i-1][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                    
                    joint=new HingeJoint(geom[k][i-1][j].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(-0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                if(j>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i][j-1].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                    joint=new HingeJoint(geom[k][i][j-1].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,-0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                if(k>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k-1][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    joint.setCollisionBetweenLinkedBodys(false);
                    
                    bulletAppState.getPhysicsSpace().add(joint);
                    joint=new HingeJoint(geom[k-1][i][j].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,-0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
               // if(Math.random()<0.5){
                    node_bloc.attachChild(geom[k][i][j]);
                    
               // }
            }
        }
    }
    node_bloc.setUserData("dynamic", true);
    obj_pierre.attachChild(node_bloc);
    rootNode.attachChild(obj_pierre);

    compteur++;
  }
 
  /** This method creates one individual physical brick. */
  public void makeBrick(Vector3f loc) {

    Node node_bloc = new Node("brick" + compteur);
    Geometry[][][] geom = new Geometry[4][4][4];
    for(int k = 0; k <4; k++){
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                Vector3f vt =
                 new Vector3f((i * brickLength*2f)+loc.x, (j*brickHeight*2f)+loc.y, (k*brickWidth*2f)+loc.z);
                //if(mat[k][i][j]){
                //System.out.println("vt = "+vt.x+" "+vt.y+" "+vt.z);

                geom[k][i][j] = new Geometry("brick", box);
                geom[k][i][j].setMaterial(wall_mat);
                geom[k][i][j].setLocalTranslation(vt);
                
                
                brick_phy = new RigidBodyControl(0.5f);
                geom[k][i][j].addControl(brick_phy);
                bulletAppState.getPhysicsSpace().add(brick_phy);
                
               
                if(i>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i-1][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                    
                    joint=new HingeJoint(geom[k][i-1][j].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(-0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                if(j>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i][j-1].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                    joint=new HingeJoint(geom[k][i][j-1].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,-0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                if(k>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k-1][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    joint.setCollisionBetweenLinkedBodys(false);
                    
                    bulletAppState.getPhysicsSpace().add(joint);
                    joint=new HingeJoint(geom[k-1][i][j].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,-0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    joint.setCollisionBetweenLinkedBodys(false);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
               // if(Math.random()<0.5){
                    node_bloc.attachChild(geom[k][i][j]);
                    
               // }
            }
        }
    }
    node_bloc.setUserData("dynamic", true);
    obj_pierre.attachChild(node_bloc);

    compteur++;

  }
 
  /** This method creates one individual physical cannon ball.
   * By defaul, the ball is accelerated and flies
   * from the camera position in the camera direction.*/
   public void detruitBloc(Vector3f dir) {
    // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        // 3. Collect intersections between Ray and Shootables in results list.
        obj_pierre.collideWith(ray, results);
        if(results.size() > 0){
            if(results.getCollision(0).getGeometry().getName().contains("brick")){
                results.getCollision(0).getGeometry().getParent().detachChild(results.getCollision(0).getGeometry());
            }
        }
   }
 
  /** A plus sign used as crosshairs to help the player with aiming.*/
  protected void initCrossHairs() {
    guiNode.detachAllChildren();
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+");        // fake crosshairs :)
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
      settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
    guiNode.attachChild(ch);
  }
  
  
      private void setupKeys() {
        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("make_brick", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addListener(this,"Lefts");
        inputManager.addListener(this,"Rights");
        inputManager.addListener(this,"Ups");
        inputManager.addListener(this,"Downs");
        inputManager.addListener(this,"Space");
        inputManager.addListener(this,"shoot");
        inputManager.addListener(this, "make_brick");
    }
      
     public void onAction(String binding, boolean value, float tpf) {

        if (binding.equals("Lefts")) {
            if(value) {
                left=true;
            }
            else {
                left=false;
            }
        } else if (binding.equals("Rights")) {
            if(value) {
                right=true;
            }
            else {
                right=false;
            }
        } else if (binding.equals("Ups")) {
            if(value) {
                up=true;
            }
            else {
                up=false;
            }
        } else if (binding.equals("Downs")) {
            if(value) {
                down=true;
            }
            else {
                down=false;
            }
        } else if (binding.equals("Space")) {
            player.jump();
        } else if (binding.equals("shoot")) {
            if(value) {
                channel.setAnim("coupPioche", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                detruitBloc(dir);
            }
        } else if (binding.equals("make_brick")){
            //obj_pierre.getChild(0).move(0, 20, 0);
            if(value){
                makeBrick(new Vector3f(cam.getLocation().x+(cam.getDirection().x*5f),cam.getLocation().y+10f,cam.getLocation().z+(cam.getDirection().z*5f)));
            }
            
        }
    }
         
    @Override
    public void simpleUpdate(float tpf) {
        
        pioche.setLocalTranslation(player.getPhysicsLocation().x + 1.0f,
                player.getPhysicsLocation().y - 1.0f,player.getPhysicsLocation().z - 3.0f);
        
        Vector3f vectorDifference = new Vector3f(cam.getLocation().subtract(pioche.getWorldTranslation()));
        pioche.setLocalTranslation(vectorDifference.addLocal(pioche.getLocalTranslation()));
        
        Quaternion worldDiff = new Quaternion(cam.getRotation().subtract(pioche.getWorldRotation()));
        pioche.setLocalRotation(worldDiff.addLocal(pioche.getLocalRotation()));
        
        pioche.move(cam.getDirection().mult(3));
        pioche.move(cam.getUp().mult(-0.8f));
        pioche.move(cam.getLeft().mult(0.6f));
        pioche.rotate(0, -90.0f, 0);
        
        
        dir = cam.getDirection();
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0,0,0);
        if(left) {
            walkDirection.addLocal(camLeft);
        }
        if(right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if(up) {
            walkDirection.addLocal(camDir);
        }
        if(down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        for(int i = 0; i < obj_pierre.getChildren().size();i++){
            CollisionResults results = new CollisionResults();
            Node node_temp = (Node) obj_pierre.getChild(i);
            if(node_temp.getUserData("dynamic")){
                
                Geometry geom_temp = (Geometry) node_temp.getChild(i);
                Vector3f vec = geom_temp.getLocalTransform().getTranslation();
                System.out.println("teste "+i+" x="+vec.x+" y = "+vec.y+" z="+vec.z);
                //geom_temp.collideWith(node_floor, results);
                if((vec.x<0.12)&&(vec.y < 0.12)&&(vec.z<0.12)){
                    for(int j = 0; j < node_temp.getChildren().size();j++){
                        Geometry g_temp = (Geometry) node_temp.getChild(j);
                        //System.out.println("teste "+g_temp.getControl(0).toString());
                        try{
                        System.out.println("teste "+g_temp.getControl(0).toString());
                        g_temp.removeControl(g_temp.getControl(0));    
//                        block_floor_phy = new RigidBodyControl(0f);
//                        /** Add physical ball to physics space. */
//                        g_temp.addControl(block_floor_phy);
//                        bulletAppState.getPhysicsSpace().add(block_floor_phy);
//                        bulletAppState.update(60);
                        }catch(Exception e){
                            System.out.println("catch teste "+i+" "+geom_temp.getLocalTranslation().x+" y ="+geom_temp.getLocalTranslation().y);
                        }
                        node_temp.setUserData("dynamic", false);
                    }
                }
            }
        }//End For
    }
    
    @Override
    public void collision(PhysicsCollisionEvent event){
        
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("coupPioche")) {
            channel.setLoopMode(LoopMode.DontLoop);
          }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        //unused
    }
}