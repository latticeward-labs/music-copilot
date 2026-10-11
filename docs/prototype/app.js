/* Review-only UI: image hotspots are the existing app controls.
   All platform/voice/service events are outside the phone, explicitly simulated.
   No audio, permission, storage, microphone or native API is requested. */
(function () {
  "use strict";
  const data = window.MUSICO_ROUTES;
  if (!data || !Array.isArray(data.nodes)) throw Error("Prototype route manifest missing");
  const nodes = Object.fromEntries(data.nodes.map(x => [x.id,x]));
  const $ = s => document.querySelector(s);
  let scenario = "GS-01",current = null, history = [], trace = [];
  let show = false;
  function assertDefined(id) {if (!nodes[id]) throw Error("Undefined prototype node: " + id);return nodes[id];}
  function addTrace(label,id) {trace.push({label,id});if(trace.length>50)trace.shift();}
  function go(id,label) {
    const dest = assertDefined(id);
    if (current) history.push(current);
    current = id; addTrace(label || "Open",id);render(dest);
  }
  function reset(key) {
    if (!data.scenarios[key]) throw Error("Unknown GS");
    scenario=key;history=[];trace=[];current=null;go(data.scenarios[key].start,"Start "+key);renderScenarioButtons();
  }
  function renderScenarioButtons() {
    const nav=$("#scenarios");nav.replaceChildren();
    for(const [key,s] of Object.entries(data.scenarios)) {
      const button=document.createElement("button");button.type="button";button.setAttribute("aria-current",String(scenario===key));
      const id=document.createElement("span");id.className="scenario-id";id.textContent=key;
      const title=document.createElement("strong");title.textContent=s.title;
      button.append(id,title);button.addEventListener("click",()=>reset(key));nav.append(button);
    }
    $("#scenario-title").textContent=data.scenarios[scenario].title;
  }
  function actionButton(edge,where) {
    const b=document.createElement("button");b.type="button";
    b.textContent=edge.label;
    b.title=(edge.kind==="tap"?"Existing approved app control":edge.kind==="system"?"External Android result simulation":"Reviewer-only deterministic event")+ (edge.note ? ": "+edge.note:"");
    b.addEventListener("click",()=>go(edge.to,edge.label));where.append(b);
    return b;
  }
  function render(node) {
    $("#state-id").textContent=node.stateId+" · "+node.wf?.toUpperCase()??"";
    // Above status includes null WF only for OS handoff; replace explicitly.
    $("#state-id").textContent=node.stateId+(node.wf?" · "+node.wf:" · SYSTEM ONLY");
    $("#state-title").textContent=node.title;
    $("#state-desc").textContent=node.summary;
    $("#state-wf").textContent="UI owner: "+node.owner+". "+(node.systemOnly?"Native boundary (not app UI).":"Source: "+node.image);
    $("#system-boundary-warning").hidden=!node.systemOnly;
    const img=$("#screen-image");
    const placeholder=$("#system-placeholder");
    img.hidden=node.systemOnly;
    placeholder.hidden=!node.systemOnly;
    if(!node.systemOnly){img.src=node.image;img.alt=node.title+" — existing high-fidelity source visual ("+node.stateId+")";}
    else {img.removeAttribute("src");img.alt="";}
    const hotspots=$("#hotspots");hotspots.replaceChildren();hotspots.classList.toggle("visible",show);
    const taps=$("#tap-actions"),events=$("#simulation-actions");taps.replaceChildren();events.replaceChildren();
    events.classList.add("simulated");
    let nTap=0,nEvent=0;
    for(const edge of node.edges) {
      assertDefined(edge.to);
      if(edge.kind==="tap") {
        nTap++;actionButton(edge,taps);
        const rect=edge.rect;const b=document.createElement("button");
        b.type="button";b.className="hotspot";
        b.setAttribute("aria-label",edge.label);b.title=edge.label;
        b.style.left=(rect[0]/360*100)+"%";b.style.top=(rect[1]/800*100)+"%";
        b.style.width=(rect[2]/360*100)+"%";b.style.height=(rect[3]/800*100)+"%";
        b.addEventListener("click",()=>go(edge.to,edge.label));hotspots.append(b);
      } else {nEvent++;actionButton(edge,events);}
    }
    if(nTap===0){const msg=document.createElement("div");msg.className="empty";msg.textContent=node.systemOnly?"No app-owned controls on this platform surface.":"No additional app control in this state.";taps.append(msg);}
    if(nEvent===0){const msg=document.createElement("div");msg.className="empty";msg.textContent="No simulated event on this step.";events.append(msg);}
    $("#context").textContent=node.systemOnly?
      "Only the external simulator resolves this native transition. When control returns, the next app state is based on an authoritative platform/service result.":
      "This is a static source screenshot and a deterministic design walkthrough. A transition to another app screenshot represents an approved user action or a simulated owner acknowledgement, not functioning Media3/STT.";
    const list=$("#journey");list.replaceChildren();
    const s=data.scenarios[scenario];
    let index=s.primary.indexOf(current);
    s.primary.forEach((id,i)=>{
      const li=document.createElement("li");li.textContent=nodes[id].title;
      if(i===index)li.className="active";else if(index>=0 && i<index)li.className="passed";
      list.append(li);
    });
    const tr=$("#trace");tr.replaceChildren();
    trace.slice(-10).forEach(x=>{let e=document.createElement("div");e.textContent=x.label+" → "+x.id;tr.append(e)});
    document.title=node.title+" · Music Copilot Prototype";
  }
  $("#back").addEventListener("click",()=>{
    if(!history.length){reset(scenario);return;}
    current=history.pop();addTrace("Reviewer Back",current);render(assertDefined(current));
  });
  $("#restart").addEventListener("click",()=>reset(scenario));
  $("#show-hotspots").addEventListener("change",e=>{show=e.target.checked;$("#hotspots").classList.toggle("visible",show)});
  window.addEventListener("keydown",e=>{
    if(e.key==="Escape" && !e.repeat && !(e.target instanceof HTMLInputElement)){
      if(history.length){current=history.pop();addTrace("Reviewer Escape",current);render(assertDefined(current));}
    }
  });
  window.addEventListener("hashchange",()=>{const k=window.location.hash.replace("#","").toUpperCase();if(data.scenarios[k])reset(k)});
  const initial=window.location.hash.replace("#","").toUpperCase();
  reset(data.scenarios[initial]?initial:"GS-01");
})();
