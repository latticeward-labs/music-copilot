#!/usr/bin/env node
"use strict";
const fs=require("node:fs"),path=require("node:path"),assert=require("node:assert/strict");
const R=require("./routes.js"),ROOT=path.resolve(__dirname,"../..");
const prev=JSON.parse(fs.readFileSync(path.join(ROOT,"docs/high-fidelity/HIFI_STATE_COVERAGE_REGISTER.json"),"utf8"));
const sys=JSON.parse(fs.readFileSync(path.join(ROOT,"docs/wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json"),"utf8"));
const phase6=JSON.parse(fs.readFileSync(path.join(ROOT,"docs/high-fidelity/specialist/phase-6/PHASE_6_REGISTER.json"),"utf8"));
const can=new Map(prev.states.map(x=>[x.id,x])),nodes=new Map(R.nodes.map(n=>[n.id,n]));
assert.equal(nodes.size,R.nodes.length,"duplicate nodes");
assert.deepEqual(Object.keys(R.scenarios),["GS-01","GS-02","GS-03","GS-04","GS-05"]);
assert.equal(prev.states.length,126);assert.equal(R.archiveSha256,prev.authority.sourceArchiveSha256);
const routeJSON=JSON.parse(fs.readFileSync(path.join(__dirname,"routes.json"),"utf8"));
assert.deepEqual(routeJSON,R,"routes.json must exactly match routes.js (run export-routes.cjs)");

function checkPNG(p,id){
  const absolute=path.resolve(__dirname,p);
  assert(absolute.startsWith(path.join(ROOT,"docs/high-fidelity/")),"external asset "+id+": "+p);
  assert(fs.existsSync(absolute),"missing PNG "+id+": "+absolute);
  const b=fs.readFileSync(absolute);
  assert.deepEqual([...b.subarray(0,8)],[137,80,78,71,13,10,26,10],"not PNG: "+p);
  assert.equal(b.readUInt32BE(16),1080,"wrong PNG width: "+p);
  assert.equal(b.readUInt32BE(20),2400,"wrong PNG height: "+p);
}
let appCount=0,sysCount=0,edges=0,hotspots=0;
for(const n of R.nodes){
  assert(can.has(n.stateId),"unknown state "+n.id+" "+n.stateId);
  assert(n.edges.length>0,"dead-end "+n.id);
  const c=can.get(n.stateId);
  if(n.systemOnly){
    ++sysCount;
    assert.equal(c.type,"SYSTEM_ONLY_HANDOFF","system node uses app ID "+n.id);
    assert.equal(n.image,null);assert.equal(n.wf,null);
    assert(sys.canonicalSystemOnly.includes(n.stateId));
    assert(n.owner.toLowerCase().includes("android"),"wrong platform owner "+n.id);
  }else{
    ++appCount;
    assert.notEqual(c.type,"SYSTEM_ONLY_HANDOFF","app mimics OS "+n.stateId);
    assert(c.wf.includes(n.wf),"wrong WF: "+n.id+" "+n.wf+" vs "+c.wf);
    assert(n.image&&n.image.startsWith("../high-fidelity/"),"wrong image "+n.id);
    checkPNG(n.image,n.id);
  }
  for(const e of n.edges){
    ++edges;assert(nodes.has(e.to),"dangling edge "+n.id+" to "+e.to);
    assert(["tap","simulate","system"].includes(e.kind),"invalid kind");
    if(n.systemOnly)assert.equal(e.kind,"system","fake app UI on system "+n.id);
    if(e.kind==="tap"){
      ++hotspots;assert(!n.systemOnly);
      assert(Array.isArray(e.rect)&&e.rect.length===4,"missing rect "+n.id);
      const [x,y,w,h]=e.rect;
      assert(x>=0&&y>=0&&x+w<=360&&y+h<=800,"offscreen hotspot "+n.id+" "+e.label);
      assert(w>=48&&h>=48,"undersize hotspot "+n.id+" "+e.label);
    }else assert(!e.rect,"simulate event masquerades as hotspot "+n.id);
  }
}
function reachable(start){
  const seen=new Set(),queue=[start];
  while(queue.length){
    const id=queue.shift();if(seen.has(id))continue;seen.add(id);
    queue.push(...nodes.get(id).edges.map(e=>e.to));
  }return seen;
}
// Guard against the older false-positive: GS-02/03/04/05 could reach most
// other scenarios through shared Player/Library and therefore pass a global
// reachability test without having a local safe recovery endpoint.
const safety=R.recoverySafety;
assert.deepEqual(Object.keys(safety),Object.keys(R.scenarios));
function within(start,allow){
  const seen=new Set(),queue=[start],paths=new Map([[start,[start]]]);
  while(queue.length){
    const id=queue.shift();if(seen.has(id))continue;seen.add(id);
    for(const e of nodes.get(id).edges){
      if(allow.has(e.to)&&!paths.has(e.to)){paths.set(e.to,paths.get(id).concat(e.to));queue.push(e.to);}
    }
  }
  return {seen,paths};
}
const coverage={};let distinct=new Set();
for(const [name,gs] of Object.entries(R.scenarios)){
  assert(nodes.has(gs.start)&&nodes.has(gs.success));
  assert(fs.existsSync(path.join(ROOT,"docs/user-flows/golden-scenarios",gs.source)));
  const policy=safety[name],allowed=new Set(policy.allowed),endpoints=new Set(policy.terminals);
  assert.equal(allowed.size,policy.allowed.length,"duplicate scoped allowed entry "+name);
  assert.equal(endpoints.size,policy.terminals.length,"duplicate terminal "+name);
  assert(allowed.has(gs.start)&&allowed.has(gs.success),name+" start/success excluded");
  assert([...endpoints].every(id=>allowed.has(id)),"out-of-scope terminal "+name);
  assert(gs.primary.every(id=>allowed.has(id)),"primary escapes "+name);
  assert(gs.recoveries.every(id=>allowed.has(id)),"recovery escapes "+name);
  const traversal=within(gs.start,allowed),seen=traversal.seen;
  assert.equal(gs.primary[0],gs.start);assert.equal(gs.primary.at(-1),gs.success);
  for(let i=0;i<gs.primary.length-1;i++){
    assert(nodes.get(gs.primary[i]).edges.some(e=>e.to===gs.primary[i+1]),"unconnected primary "+name+" "+gs.primary[i]);
    distinct.add(gs.primary[i]);distinct.add(gs.primary[i+1]);
  }
  const safePaths={};
  for(const id of gs.recoveries){
    assert(seen.has(id),"recovery unreachable INSIDE scenario "+name+" "+id);distinct.add(id);
    const route=within(id,allowed);
    const eligible=[...endpoints].filter(e=>route.paths.has(e)).map(e=>({end:e,path:route.paths.get(e)}));
    assert(eligible.length,"No safe endpoint within declared scenario: "+name+" "+id);
    eligible.sort((a,b)=>a.path.length-b.path.length);
    safePaths[id]=eligible[0];
    assert(safePaths[id].path.every(n=>allowed.has(n)),"cross-scenario escape "+name+" "+id);
  }
  coverage[name]={primarySteps:gs.primary.length,scopedReachableNodes:seen.size,
    recoveryCount:gs.recoveries.length,safeTerminalPaths:safePaths,
    primaryConnected:true,missing:[],crossScenarioRecoveryUsed:false};
}
const at=id=>nodes.get(id);
for(const id of ["driving","driving-paused","driving-none","driving-last","driving-safe"]){
  assert(at(id).edges.some(e=>e.label.includes("Exit")),"missing immediate Exit "+id);
  assert(at(id).edges.every(e=>!["search","queue","settings","privacy","consent"].includes(e.to)),"deep Driving nav "+id);
}
assert(at("driving-none").edges.every(e=>!e.label.includes("Play last")));
assert(at("driving-last").edges.some(e=>e.label.includes("Play last")));
assert(at("voice-timeout").edges.some(e=>e.label==="Retry once"&&e.to==="voice-listen-two"));
for(const id of ["voice-terminal","voice-not-found","voice-unsupported","voice-unknown","voice-rejected","voice-provider","voice-permission"]){
  assert(at(id).edges.every(e=>!e.label.toLowerCase().includes("retry")),"invalid Retry "+id);
}
assert(!at("voice-confirm-one").edges.some(e=>e.label.includes("option 2")));
assert.equal(at("voice-confirm-two").edges.filter(e=>e.label.includes("Confirm option ")).length,2);
assert(at("voice-done").edges.some(e=>e.to==="voice-tts"));
assert(at("voice-tts").edges.every(e=>e.to==="driving"));
assert(at("route-return").edges.every(e=>e.to!=="player"),"auto-resume on reconnect");
for(const id of ["session-handoff","background","screen-off","sys-controls","lock-controls","external-keys","lifecycle-event"]){
  assert(at(id).systemOnly,"GS-03 fabricated system screen: "+id);
}
assert(at("screen-off").stateId==="CB-24" && at("session-handoff").stateId==="CB-22",
       "GS-03 skips required active-session/screen-off boundaries");
assert(at("empty").edges.some(e=>e.kind==="tap" && e.label==="Rescan" &&
  e.rect[1]>=350 && e.rect[1]<=390),"Empty Library Rescan misaligned");

assert(at("manual-resume").edges.some(e=>e.to==="player"));
const html=fs.readFileSync(path.join(__dirname,"index.html"),"utf8");
const js=fs.readFileSync(path.join(__dirname,"app.js"),"utf8");
const css=fs.readFileSync(path.join(__dirname,"styles.css"),"utf8");
for(const value of ["routes.js","app.js","styles.css","scenarios","hotspots","system-placeholder","simulation-actions"])
  assert(html.includes(value),"HTML missing "+value);
assert(!/https?:\/\/[^"']+(\.js|\.css)/.test(html),"external CDN dependency");
for(const blocked of ["navigator.mediaDevices","webkitSpeechRecognition","SpeechRecognition(","AudioContext(","MediaSessionService(","localStorage","fetch("])
  assert(!js.includes(blocked),"prototype performs real sensitive operation "+blocked);
assert(js.includes("node.systemOnly")&&js.includes("edge.kind")&&css.includes(".hotspot"));
assert.equal(phase6.validation.maxRetry,1);
assert.equal(phase6.validation.ufStateCoverage,42);
const report={issue:17,phase:8,date:"2026-10-11",status:"PASS_SOURCE_GRAPH_AND_ASSET_VALIDATION",
  archiveSha256:R.archiveSha256,scenarios:coverage,nodeCount:R.nodes.length,appSurfaceNodes:appCount,
  annotatedSystemOnlyNodes:sysCount,edges,interactiveHotspots:hotspots,
  uniquePrimaryAndRecoveryNodes:distinct.size,allImagesExistingAndValidPng:true,
  fiveGoldenPathsConnected:true,allRequiredRecoveriesReachable:true,allRecoveryEndpointsProvenInOwnScenario:true,
  noFakeAndroidSystemImages:true,oneRetryOneConfirmationNoApproximation:true,
  noAutomaticRouteResume:true,drivingExitAlwaysAvailable:true,
  privacyNoNetworkOrVoiceRuntimeCalls:true,missing:[],duplicates:[],unassigned:[],
  browserVisualQA:"SEE_SEPARATE_CHROMIUM_BROWSER_VALIDATION",humanSignoff:"PENDING",androidRuntimeQA:"NOT_RUN"};
fs.writeFileSync(path.join(__dirname,"PHASE_8_VALIDATION.json"),JSON.stringify(report,null,2)+"\n");
console.log("PASS",JSON.stringify(report));
