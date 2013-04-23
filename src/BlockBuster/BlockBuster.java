package BlockBuster;
 
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
 
/**
 * Example 12 - how to give objects physical properties so they bounce and fall.
 * @author base code by double1984, updated by zathras
 */
public class BlockBuster extends SimpleApplication implements ActionListener {
 
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
 
  /** dimensions used for bricks and wall */
  private static final float brickLength = 0.1f;
  private static final float brickWidth  = 0.1f;
  private static final float brickHeight = 0.1f;
  
   private Node gameLevel;
   private PhysicsCharacter player;
   private Vector3f walkDirection = new Vector3f();
   private static boolean useHttp = false;
   private boolean left=false,right=false,up=false,down=false;
   
   private Node[] obj_pierre = new Node[10000];
   private int compteur;
   private Geometry[][][] geom = new Geometry[5][5][5];
   private Vector3f dir;
  /** Teste node objet **/
  //Node obj_pierre;
  
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
           
    /** Set up Physics Game */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    compteur = 0;
    /** Configure cam to look at scene */
    //cam.setLocation(new Vector3f(0, 4f, 6f));
    //cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
    /** Initialize the scene, materials, and physics space */
    initMaterials();
    initWall(new Vector3f(0,10,0));
    //obj_pierre.move(10,0,0);
    //initWall(new Vector3f(10,0,0));
    initFloor();
    initCrossHairs();


    flyCam.setMoveSpeed(100);
    setupKeys();
    this.cam.setFrustumFar(2000);

     player = new PhysicsCharacter(new SphereCollisionShape(5), .1f);
     player.setJumpSpeed(20);
     player.setFallSpeed(30);
     player.setGravity(20);

     player.setPhysicsLocation(new Vector3f(0, 10, 10));

//     rootNode.attachChild(gameLevel);

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
    Geometry floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setLocalTranslation(0, -0.1f, 0);
    this.rootNode.attachChild(floor_geo);
    /* Make the floor physical with mass 0.0f! */
    floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    bulletAppState.getPhysicsSpace().add(floor_phy);
  }
 
  /** This loop builds a wall out of individual bricks. */
  public void initWall(Vector3f loc) {
    obj_pierre[compteur] = new Node("object_"+compteur+""); 

//    boolean  mat[][][] = new boolean[11][11][11];
//    for(int k = 0; k <11; k++){
//        for (int j = 0; j < 11; j++) {
//          for (int i = 0; i < 11; i++) {
//              mat[i][j][k]=false;
//          }
//        }
//    }
//    mat[5][5][5]=true;
//    for(int k = 4; k >1; k--){
//        for (int j = 9; j > 1; j--) {
//          for (int i = 9; i > 1; i--) {
//               if((mat[k-1][i][j]==true)||(mat[k+1][i][j]==true)||(mat[k][i-1][j]==true)||(mat[k][i+1][j]==true)||(mat[k][i][j+1]==true)||(mat[k][i][j-1]==true)){
//                   if(Math.random()>0.25){
//                       mat[k][i][j] = true;
//                   }
//               }
//            }
//        }
//    }
//    for(int k = 6; k < 10; k++){
//        for (int j = 9; j > 1; j--) {
//          for (int i = 9; i > 1; i--) {
//               if((mat[k-1][i][j]==true)||(mat[k+1][i][j]==true)||(mat[k][i-1][j]==true)||(mat[k][i+1][j]==true)||(mat[k][i][j+1]==true)||(mat[k][i][j-1]==true)){
//                   if(Math.random()>0.25){
//                       mat[k][i][j] = true;
//                   }
//               }
//            }
//        }
//    }
   // Geometry[][][] geom = new Geometry[5][5][5];
    for(int k = 0; k <5; k++){
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                Vector3f vt =
                 new Vector3f((i * brickLength*2)+loc.x, (j*brickHeight*2)+loc.y, (k*brickWidth*2)+loc.z);
                //if(mat[k][i][j]){
                //System.out.println("vt = "+vt.x+" "+vt.y+" "+vt.z);
                geom[k][i][j] = new Geometry("brick", box);
                geom[k][i][j].setMaterial(wall_mat);
                geom[k][i][j].setLocalTranslation(vt);
                
                
//                brick_phy = new RigidBodyControl(0.5f);
//                geom[k][i][j].addControl(brick_phy);
//                bulletAppState.getPhysicsSpace().add(brick_phy);
                obj_pierre[compteur].attachChild(geom[k][i][j]);

               
//                if(i>0){
//                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i-1][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
//                    bulletAppState.getPhysicsSpace().add(joint);
//                }
//                if(j>0){
//                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i][j-1].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
//                    bulletAppState.getPhysicsSpace().add(joint);
//                }
//                if(k>0){
//                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k-1][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
//                    bulletAppState.getPhysicsSpace().add(joint);
//                }
                
            }
        }
    }

    for(int i = 0; i < obj_pierre[compteur].getQuantity();i++){
        if(Math.random()<0.5){
            obj_pierre[compteur].getChild(i).removeFromParent();
        }
    }
    block_phy = new RigidBodyControl(0.5f);
    
    obj_pierre[compteur].addControl(block_phy);
    
    bulletAppState.getPhysicsSpace().add(block_phy);
    
    rootNode.attachChild(obj_pierre[compteur]);
    //CompoundCollisionShape compound=new CompoundCollisionShape();
    //BoxCollisionShape boxCollisionShape=new BoxCollisionShape(new Vector3f(1f,1f,1f));
    //SphereCollisionShape sphereCollisionShape = new SphereCollisionShape(1f);
    //CollisionShape myComplexShape = CollisionShapeFactory.createMeshShape((Node) obj_pierre );
    //compound.addChildShape(sphereCollisionShape, new Vector3f(1f,1f,1f));
    //brick_phy = new RigidBodyControl(myComplexShape,2f);
    //obj_pierre.setLocalTranslation(0,2,0);
    //obj_pierre.addControl(brick_phy);
    
    //obj_pierre.getControl(RigidBodyControl.class).getCollisionShape().setScale(new Vector3f(,5,5));

    //bulletAppState.getPhysicsSpace().add(brick_phy);
    compteur++;
  }
 
  /** This method creates one individual physical brick. */
  public void makeBrick(Vector3f loc) {
    /** Create a brick geometry and attach to scene graph. */
    obj_pierre[compteur] = new Node("object_"+compteur+""); 
    //Geometry[][][] geom = new Geometry[5][5][5];
    for(int k = 0; k <5; k++){
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                Vector3f vt =
                 new Vector3f((i * brickLength*2)+loc.x, (j*brickHeight*2)+loc.y, (k*brickWidth*2)+loc.z);
                //if(mat[k][i][j]){
                //System.out.println("vt = "+vt.x+" "+vt.y+" "+vt.z);
                //geom[k][i][j] = new Geometry("brick", box);
                geom[k][i][j].setMaterial(wall_mat);
                //geom[k][i][j].setLocalTranslation(vt);
                
                brick_phy = new RigidBodyControl(1f);
                //obj_pierre[compteur]
                geom[k][i][j].addControl(brick_phy);
                
                bulletAppState.getPhysicsSpace().add(brick_phy);
                obj_pierre[compteur].attachChild(geom[k][i][j]);
               
                if(i>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i-1][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    bulletAppState.getPhysicsSpace().add(joint);
                    joint=new HingeJoint(geom[k][i-1][j].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(-0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                if(j>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i][j-1].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    bulletAppState.getPhysicsSpace().add(joint);
                    joint=new HingeJoint(geom[k][i][j-1].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,-0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                if(k>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k-1][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    bulletAppState.getPhysicsSpace().add(joint);
                    joint=new HingeJoint(geom[k-1][i][j].getControl(RigidBodyControl.class), geom[k][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,-0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
            }
        }
    }

    for(int i = 0; i < obj_pierre[compteur].getQuantity();i++){
        if(Math.random()<0.5){
            obj_pierre[compteur].getChild(i).removeFromParent();
        }
    }
    
    rootNode.attachChild(obj_pierre[compteur]);
    compteur++;

  }
 
  /** This method creates one individual physical cannon ball.
   * By defaul, the ball is accelerated and flies
   * from the camera position in the camera direction.*/
   public void makeCannonBall(Vector3f dir) {
    /** Create a cannon ball geometry and attach to scene graph. */
    Geometry ball_geo = new Geometry("cannon ball", sphere);
    ball_geo.setMaterial(stone_mat);
    rootNode.attachChild(ball_geo);
    /** Position the cannon ball  */
    System.out.println(cam.getLocation());
    ball_geo.setLocalTranslation(cam.getLocation().x+(cam.getDirection().x*5f),cam.getLocation().y,cam.getLocation().z+(cam.getDirection().z*5f));
    /** Make the ball physcial with a mass > 0.0f */
    ball_phy = new RigidBodyControl(1f);

    /** Add physical ball to physics space. */
    ball_geo.addControl(ball_phy);
   
    bulletAppState.getPhysicsSpace().add(ball_phy);
    
    /** Accelerate the physcial ball to shoot it. */
    System.out.println(dir);
    ball_phy.setLinearVelocity(dir.mult(25));
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
                makeCannonBall(dir);
            }
        } else if (binding.equals("make_brick")){
            //obj_pierre.getChild(0).move(0, 20, 0);
            initWall(new Vector3f(cam.getLocation().x+(cam.getDirection().x*5f),cam.getLocation().y,cam.getLocation().z+(cam.getDirection().z*5f)));
        }
    }
         
    @Override
    public void simpleUpdate(float tpf) {
        //System.out.println(cam.getDirection().clone().multLocal(0.6f));
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
    }
}