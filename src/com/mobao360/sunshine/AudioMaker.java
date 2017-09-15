package com.mobao360.sunshine;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioMaker extends Activity {
    /** Called when the activity is first created. */
    static  int frequency = 8000;//�ֱ���  
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;  
    static final int audioEncodeing = AudioFormat.ENCODING_PCM_16BIT; 
    static final int yMax = 50;//Y����С�������ֵ  
    static final int yMin = 1;//Y����С������Сֵ  
	
	int minBufferSize;//�ɼ�������Ҫ�Ļ�������С
	AudioRecord audioRecord;//¼��
	AudioProcess audioProcess = new AudioProcess();//����
	
    Button btnStart,btnExit;  //��ʼֹͣ��ť
    SurfaceView sfv;  //��ͼ����
    ZoomControls zctlX,zctlY;//Ƶ��ͼ����
    Spinner spinner;//�����˵�
    ArrayList<String> list=new ArrayList<String>();
    ArrayAdapter<String>adapter;//�����˵�������
    TextView tView;
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initControl();
        }
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
    
  //��ʼ���ؼ���Ϣ
    private void initControl() {
    	//��ȡ������
        tView = (TextView)this.findViewById(R.id.tvSpinner);
        spinner = (Spinner)this.findViewById(R.id.spinnerFre);
        String []ls =getResources().getStringArray(R.array.action);
        for(int i=0;i<ls.length;i++){
        	list.add(ls[i]);
        }
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("��ѡ�������");
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
        	@SuppressWarnings("unchecked")
        	public void onItemSelected(AdapterView arg0,View agr1,int arg2,long arg3){
        		frequency = Integer.parseInt(adapter.getItem(arg2));
        		tView.setText("��ѡ�����:"+adapter.getItem(arg2)+"HZ");
        		Log.i("sunshine",String.valueOf(minBufferSize));
        		arg0.setVisibility(View.VISIBLE);
        	}
        	@SuppressWarnings("unchecked")
        	public void onNothingSelected(AdapterView arg0){
        		arg0.setVisibility(View.VISIBLE);
        	}
        });
        
        
        Context mContext = getApplicationContext();
        //����
        btnStart = (Button)this.findViewById(R.id.btnStart);
        btnExit = (Button)this.findViewById(R.id.btnExit);
        //�����¼�����
        btnStart.setOnClickListener(new ClickEvent());
        btnExit.setOnClickListener(new ClickEvent());
        //���ʺͻ���
        sfv = (SurfaceView)this.findViewById(R.id.SurfaceView01);
        //��ʼ����ʾ
        audioProcess.initDraw(yMax/2, sfv.getHeight(),mContext,frequency);
        //��������
        zctlY = (ZoomControls)this.findViewById(R.id.zctlY);
        zctlY.setOnZoomInClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if(audioProcess.rateY - 5>yMin){
                	audioProcess.rateY = audioProcess.rateY - 5;  
	                setTitle("Y����С"+String.valueOf(audioProcess.rateY)+"��");
                }else{
                	audioProcess.rateY = 1;
	                setTitle("ԭʼ�ߴ�");
                }
            }  
        });  
          
        zctlY.setOnZoomOutClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if(audioProcess.rateY<yMax){
                	audioProcess.rateY = audioProcess.rateY + 5;      
	                setTitle("Y����С"+String.valueOf(audioProcess.rateY)+"��");  
                }else {
                	setTitle("Y���Ѿ���������С");
				}
            }  
        });
	}
    
    /**
     * �����¼�����
     */
    class ClickEvent implements View.OnClickListener{
    	@Override
    	public void onClick(View v){
    		Button button = (Button)v;
    		if(button == btnStart){
    			if(button.getText().toString().equals("Start")){
        	        try {
            			//¼��
            	        minBufferSize = AudioRecord.getMinBufferSize(frequency, 
            	        		channelConfiguration, 
            	        		audioEncodeing);
            	        //minBufferSize = 2 * minBufferSize; 
            	        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,frequency,
            	        		channelConfiguration,
            	        		audioEncodeing,
            	        		minBufferSize);
            			audioProcess.baseLine = sfv.getHeight()-100;
            			audioProcess.frequence = frequency;
            			audioProcess.start(audioRecord, minBufferSize, sfv);
            			Toast.makeText(AudioMaker.this, 
            					"��ǰ�豸֧������ѡ��Ĳ�����:"+String.valueOf(frequency), 
            					Toast.LENGTH_SHORT).show();
            			btnStart.setText(R.string.btn_exit);
            	        spinner.setEnabled(false);
    				} catch (Exception e) {
    					// TODO: handle exception
            			Toast.makeText(AudioMaker.this, 
            					"��ǰ�豸��֧������ѡ��Ĳ�����"+String.valueOf(frequency)+",������ѡ��", 
            					Toast.LENGTH_SHORT).show();
    				}
        		}else if (button.getText().equals("Stop")) {
        			spinner.setEnabled(true);
    				btnStart.setText(R.string.btn_start);
    				audioProcess.stop(sfv);
    			}
    		}
    		else {
    			new AlertDialog.Builder(AudioMaker.this) 
    	         .setTitle("��ʾ") 
    	         .setMessage("ȷ���˳�?") 
    	         .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() { 
    	        public void onClick(DialogInterface dialog, int whichButton) { 
    	        setResult(RESULT_OK);//ȷ����ť�¼� 
 				AudioMaker.this.finish();
    	         finish(); 
    	         } 
    	         }) 
    	         .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() { 
    	        public void onClick(DialogInterface dialog, int whichButton) { 
    	         //ȡ����ť�¼� 
    	         } 
    	         }) 
    	         .show();
			}
    		
    	}
    }
}