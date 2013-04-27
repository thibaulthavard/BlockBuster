package BlockBuster;
 
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
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
 * BLOCKBUSTER
 * Ce jeu vous permet de créer des cubes de différentes forme à l'aide de la touche A puis de les sculter
 * Auteur : HAVARD Thibault VANDENHOVE Pierre
 * Version : 1.0.0
 * Date : 26.04.2013
 */
public class BlockBuster extends SimpleApplication 
implements ActionListener, AnimEventListener {
 
  /** Fonction Main pour lancer l'application **/
  public static void main(String args[]) {
    BlockBuster app = new BlockBuster();
    app.start();
  }
 
  /** Preparation de la physique(jBullet) */
  private BulletAppState bulletAppState;
 
  /** Preparation des Materials */
  Material wall_mat;
  Material stone_mat;
  Material floor_mat;
 
  /** Préparation des géométries et volume de control*/
  private RigidBodyControl    brick_phy;
  private RigidBodyControl    brick_stat_phy;
  private static final Box    box;
  private RigidBodyControl    floor_phy;
  private static final Box    floor;
 
  /** Dimension d'un sous-bloc */
  private static final float brickLength = 0.1f;
  private static final float brickWidth  = 0.1f;
  private static final float brickHeight = 0.1f;
  
  /** Initialisation des variables utilisateurs joueur,camera ... **/
   private PhysicsCharacter player;
   private Vector3f walkDirection = new Vector3f();
   private boolean left=false,right=false,up=false,down=false;
   
   Geometry floor_geo;
   private Node node_floor ;
   private Node obj_pierre = new Node("node_bricks");
   private int compteur,tick_compt = 1;

   private Vector3f dir;

  
   /*Pioche*/
   private Spatial pioche;
   private AnimChannel channel;
   private AnimControl control;
    
  static {

    /**Initialisation des géométries */
    box = new Box(Vector3f.ZERO, brickLength, brickHeight, brickWidth);
    box.scaleTextureCoordinates(new Vector2f(0.5f, 0.5f));
    floor = new Box(Vector3f.ZERO, 100f, 0.1f, 100f);
    floor.scaleTextureCoordinates(new Vector2f(100, 100));
  }
  
  
 
  @Override
  /** Fonction d'initialisation de l'application **/
  public void simpleInitApp() {
   
    /** Mise en place de la physique */
    bulletAppState = new BulletAppState();
    
    bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
    
    stateManager.attach(bulletAppState);
    bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    compteur = 0;
    
    //Initialisation de l'environnement
    initMaterials();
    initWall(new Vector3f(10,0,0));
    initFloor();
    initCrossHairs();

    //Initialisation Controle et personnage
    setupKeys();
    
    player = new PhysicsCharacter(new SphereCollisionShape(5), .1f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);
    player.setGravity(20);
    player.setPhysicsLocation(new Vector3f(0, 10, 10));
     
     pioche = assetManager.loadModel("Models/pioche.j3o");
     pioche.scale(0.3f, 0.3f, 0.3f);
     rootNode.attachChild(pioche);
     
     control = rootNode.getChild("Pioche-ogremesh").getControl(AnimControl.class);
     control.addListener(this);
     channel = control.createChannel();
                 
     bulletAppState.getPhysicsSpace().add(player);
      
  }

 
  /** Initialisation des matériaux */
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
 
  /** Création du sol */
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
 
  /** Création du premier blocs lors du debut du jeu */
  public void initWall(Vector3f loc) {
    Node node_bloc = new Node("brick" + compteur);
    Geometry[][][] geom = new Geometry[4][4][4];
    for(int k = 0; k <4; k++){
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                Vector3f vt =
                 new Vector3f((i * brickLength*2f)+loc.x, (j*brickHeight*2f)+loc.y, (k*brickWidth*2f)+loc.z);

                geom[k][i][j] = new Geometry("brick", box);
                geom[k][i][j].setMaterial(wall_mat);
                geom[k][i][j].setLocalTranslation(vt);             
                
                brick_phy = new RigidBodyControl(0.1f);
                geom[k][i][j].addControl(brick_phy);
                bulletAppState.getPhysicsSpace().add(brick_phy);
                
                //Jonction des blocs entre eux
                if(i>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i-1][j].getControl(RigidBodyControl.class), Vector3f.ZERO,new Vector3f(0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    bulletAppState.getPhysicsSpace().add(joint);;
                }
                if(j>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i][j-1].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                if(k>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k-1][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    bulletAppState.getPhysicsSpace().add(joint);

                }
                float prob = 0;
                if((k<=2)&&(k>=1)){
                    prob += 0.3;
                }else{
                    prob +=0.1;
                }
                if((j<=2)&&(j>=1)){
                    prob += 0.3;
                }else{
                    prob +=0.1;
                }
                if((i<=2)&&(i>=1)){
                    prob += 0.3;
                }else{
                    prob +=0.1;
                }
                if(Math.random()<prob){
                    node_bloc.attachChild(geom[k][i][j]);      
               }
            }
        }
    }
    node_bloc.setUserData("dynamic", true);
    node_bloc.setUserData("pos", loc);
    node_bloc.setUserData("pos2", Vector3f.ZERO);
    obj_pierre.attachChild(node_bloc);
    rootNode.attachChild(obj_pierre);


    compteur++;
  }
 
  /** Cette fonction créer un nouveau bloc */
  public void makeBrick(Vector3f loc) {

    Node node_bloc = new Node("brick" + compteur);
    Geometry[][][] geom = new Geometry[4][4][4];
    for(int k = 0; k <4; k++){
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                Vector3f vt =
                 new Vector3f((i * brickLength*2f)+loc.x, (j*brickHeight*2f)+loc.y, (k*brickWidth*2f)+loc.z);

                geom[k][i][j] = new Geometry("brick", box);
                geom[k][i][j].setMaterial(wall_mat);
                geom[k][i][j].setLocalTranslation(vt);
                
                brick_phy = new RigidBodyControl(0.1f);
                geom[k][i][j].addControl(brick_phy);
                bulletAppState.getPhysicsSpace().add(brick_phy);
               
                if(i>0){
                    HingeJoint joint= new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class),
                                          geom[k][i-1][j].getControl(RigidBodyControl.class), Vector3f.ZERO,
                                          new Vector3f(0.2f,0f,0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
                    bulletAppState.getPhysicsSpace().add(joint);

                }
                if(j>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k][i][j-1].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0.2f,0f), Vector3f.UNIT_Y, Vector3f.UNIT_Y);
                    bulletAppState.getPhysicsSpace().add(joint);

                }
                if(k>0){
                    HingeJoint joint=new HingeJoint(geom[k][i][j].getControl(RigidBodyControl.class), geom[k-1][i][j].getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0f,0f,0.2f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
                    bulletAppState.getPhysicsSpace().add(joint);
                }
                float prob = 0;
                if((k<=2)&&(k>=1)){
                    prob += 0.3;
                }else{
                    prob +=0.1;
                }
                if((j<=2)&&(j>=1)){
                    prob += 0.3;
                }else{
                    prob +=0.1;
                }
                if((i<=2)&&(i>=1)){
                    prob += 0.3;
                }else{
                    prob +=0.1;
                }
                if(Math.random()<prob){
                    node_bloc.attachChild(geom[k][i][j]);      
               }
            }
        }
    }
    node_bloc.setUserData("dynamic", true);
    node_bloc.setUserData("pos", loc);
    node_bloc.setUserData("pos2", Vector3f.ZERO);
    obj_pierre.attachChild(node_bloc);
    compteur++;
  }
 
  /** Cette fonction sert à detruire le bloc qui correspond*/
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
 
  /** Cette fonction initialise la croix de visée*/
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
  
  /** Fonction d'initialisation des touches **/
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
    
    /** Fonction qui ecoute les actions effectué (ActionListener) **/
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
    /** Fonction de mise à jour et de controle. C'edt dans cette fonction qu'on passera les blocs en dynamique/statique**/
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

        //Vérification des noeuds
        for(int i = 0; i < obj_pierre.getChildren().size();i++){
            Node node_temp = (Node) obj_pierre.getChild(i);
            //Si il possède des enfants(des blocs) et que le bloc est dynamique on vérifie ses mouvements
            if((node_temp.getChildren().size()>0)&&(node_temp.getUserData("dynamic").equals(true))){
                Geometry geom_temp = (Geometry) node_temp.getChild(0);
                //Sauvegarde des positions à différents instants
                if(tick_compt==10){
                    node_temp.setUserData("pos2", geom_temp.getWorldTranslation().clone());
                }
                if(tick_compt==20){
                    node_temp.setUserData("pos", geom_temp.getWorldTranslation().clone());
                }
                //Vérification de la distance entre les deux instants
                Vector3f vec = (Vector3f)node_temp.getUserData("pos");
                Vector3f vec_prec = (Vector3f)node_temp.getUserData("pos2");
                float vec_move = vec.distance(vec_prec);
                if(vec_move < 0.009){
                    for(int j = 0; j < node_temp.getChildren().size();j++){
                        Geometry g_temp = (Geometry) node_temp.getChild(j);
                        try{
                            g_temp.removeControl(g_temp.getControl(0));
                            brick_stat_phy = new RigidBodyControl(0.0f);
                            g_temp.addControl(brick_stat_phy);
                            bulletAppState.getPhysicsSpace().add(brick_stat_phy);
                        }catch(Exception e){
                            System.out.println("Exception : pas de contrôle");
                        }
                        node_temp.setUserData("dynamic", false);
                    }
                }
            }//End For
        }
        tick_compt++;
        if(tick_compt>20){
            tick_compt = 1;
        }
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