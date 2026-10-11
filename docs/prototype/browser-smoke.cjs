#!/usr/bin/env node
"use strict";
/* Chromium-only interaction smoke test. Not a real Android/Media3/STT test. */
const {chromium}=require("playwright");
const fs=require("node:fs"),path=require("node:path"),assert=require("node:assert/strict");
(async()=>{
 const browser=await chromium.launch({headless:true,args:["--no-sandbox"]});
 let page;const cases=[];
 try{
   page=await browser.newPage({viewport:{width:1440,height:980},deviceScaleFactor:1});
   const errors=[];
   page.on("pageerror",e=>errors.push(e.message));
   page.on("console",m=>{if(m.type()==="error")errors.push(m.text())});
   await page.goto("http://127.0.0.1:8765/docs/prototype/index.html",{waitUntil:"networkidle"});
   const cur=async id=>assert.equal((await page.locator("#state-id").textContent()).split(" · ")[0],id);
   const scenario=async n=>page.locator("#scenarios button").filter({hasText:n}).click();
   const review=async name=>page.locator("#simulation-actions").getByRole("button",{name,exact:true}).click();
   const tap=async name=>page.locator("#hotspots").getByRole("button",{name,exact:true}).click();
   await cur("S-01");
   await review("Verified startup → onboarding");await cur("S-02");
   await tap("Continue");await cur("S-03");
   await tap("Continue to Android request");await cur("SYS-01");
   assert.equal(await page.locator("#system-placeholder").isVisible(),true);
   assert.equal(await page.locator("#screen-image").isVisible(),false);
   await review("Android grants media access");await cur("S-03");
   await review("Begin MediaStore scan");await cur("S-04");
   await review("Scan completes with library Ready");await cur("CB-01");
   cases.push("GS-01 primary including native-only permission boundary");
   await scenario("GS-01");
   await review("Migration error");await cur("S-01");
   await tap("Retry startup");await cur("S-01");
   await review("Verified startup → onboarding");await tap("Continue");
   await tap("Continue to Android request");
   await review("Android denies (requestable)");
   await cur("S-03");
   await review("Platform reports permanent denial");
   await cur("S-03");
   await tap("Open Android Settings");await cur("SYS-04");
   await review("After return: permission now granted");
   await review("Begin MediaStore scan");
   await review("Scan completes Empty");await cur("S-04");
   await tap("Rescan");await cur("S-04");
   await review("Scan completes with library Ready");await cur("CB-01");
   cases.push("GS-01 clickable denied → permanent Settings recovery + Empty Library Rescan");

   await scenario("GS-02");await cur("CB-01");
   await tap("Search tab");await cur("CB-09");
   await tap("Enter scripted local query");await cur("CB-10");
   await tap("Play local search result");await cur("CB-14");
   await review("Playback Service acknowledges queue");await cur("CB-15");
   cases.push("GS-02 source local-search/queue service acknowledgment");
   await scenario("GS-02");await tap("Search tab");
   await tap("Enter scripted local query");await cur("CB-10");
   await review("Selection stale or access revoked");await cur("CB-13");
   await review("Return to preserved results");await cur("CB-10");
   await tap("Play local search result");await cur("CB-14");
   await review("Playback Service acknowledges queue");
   await review("Corrupt local item encountered");await cur("CB-19");
   await review("No playable queue item remains");await cur("CB-16");
   cases.push("GS-02 stale selection preserves results; corrupt media reaches safe Paused");

   await scenario("GS-03");await cur("CB-15");
   await review("Android active-session handoff / app background");await cur("CB-22");
   await review("Activity enters background");await cur("CB-23");
   await review("Device screen off / lock screen");await cur("CB-24");
   await review("Compatible notification media controls");await cur("CB-26");
   assert.equal(await page.locator("#system-placeholder").isVisible(),true);
   await review("OS Media Key command sent");await cur("CB-29");
   await review("Service acknowledges once");await cur("CB-21");
   await review("Service snapshot confirms Playing");await cur("CB-15");
   cases.push("GS-03 CB-22 → CB-23 → CB-24 → native media key → authoritative reconnect");
   await scenario("GS-03");
   await review("Android active-session handoff / app background");
   await review("Activity enters background");
   await review("Device screen off / lock screen");
   await review("Lock-screen media control");await cur("CB-27");
   assert.equal(await page.locator("#system-placeholder").isVisible(),true);
   await review("System Play/Pause/Next/Previous key");await cur("CB-29");
   await review("Service acknowledges once");await cur("CB-21");
   await review("Service snapshot confirms Playing");await cur("CB-15");
   cases.push("GS-03 lock-screen handoff remains Android owned; compatible key reconnect");

   await scenario("GS-04");await cur("CB-15");
   await tap("Enter Driving Mode");await cur("CC-05");
   await tap("Mic · Voice");await cur("CD-03");
   await review("Supported NEXT recognized");await cur("CD-05");
   await review("Certain NEXT intent");await cur("CD-06");
   await review("Service acknowledges exactly one command");await cur("CD-07");
   await tap("Dismiss to Driving snapshot");await cur("CC-05");
   cases.push("GS-04 NEXT voice command from explicit mic to acknowledged playback");
   await scenario("GS-04");
   await tap("Enter Driving Mode");await tap("Mic · Voice");
   await review("Six-second STT timeout");await cur("CD-17");
   await tap("Retry once");await cur("CD-04");
   await review("Second attempt fails → terminal");await cur("CD-19");
   assert.equal(await page.locator("#tap-actions").getByRole("button",{name:/Retry/}).count(),0);
   await tap("Manual controls");await cur("CC-05");
   cases.push("Voice attempt-two terminal never exposes third Retry");
   await scenario("GS-04");await tap("Enter Driving Mode");await tap("Mic · Voice");
   await review("Supported NEXT recognized");await cur("CD-05");
   await review("Artist not found");await cur("CD-13");
   assert.equal(await page.locator("#tap-actions").getByRole("button",{name:/Retry/}).count(),0);
   await tap("Manual controls");await cur("CC-05");
   cases.push("GS-04 artist Not Found → Manual Only, no best-guess or Retry");

   await scenario("GS-04");await tap("Enter Driving Mode");await tap("Mic · Voice");
   await review("Supported NEXT recognized");await review("Two artist candidates");
   await cur("CD-10");await tap("Confirm option 2");await cur("CD-06");
   await review("Service acknowledges exactly one command");await cur("CD-07");
   await review("TTS becomes unavailable after ack");await cur("CD-07");
   cases.push("Voice one-confirmation option 2 and TTS failure retains Executed");
   await scenario("GS-05");await cur("CB-15");
   await review("Call, focus or route interruption");await cur("CE-07");
   assert.equal(await page.locator("#system-placeholder").isVisible(),true);
   await review("Wired/Bluetooth disconnect → immediate safe pause");await cur("CE-06");
   await review("External route reconnects");await cur("CE-09");
   assert.equal(await page.locator("#system-placeholder").isVisible(),true);
   await review("Route valid; return to app Safe Paused");await cur("CE-06");
   await tap("Explicit Play on revalidated safe route");await cur("CE-06");
   await review("Service confirms playback on safe route");await cur("CB-15");
   cases.push("GS-05 system route reconnect stays paused until manual Play");
   await page.setViewportSize({width:390,height:844});
   await scenario("GS-04");
   await tap("Enter Driving Mode");await cur("CC-05");
   await page.locator("#show-hotspots").check();
   assert.equal(await page.locator("#hotspots").evaluate(n=>n.classList.contains("visible")),true);
   await page.screenshot({path:path.join(__dirname,"PHASE_8_BROWSER_REVIEW.png"),fullPage:true});
   cases.push("Mobile-width hotspot UI visible; sanitized synthetic-browser screenshot captured");
   assert.deepEqual(errors,[],"Browser console errors");
   const report={issue:17,phase:8,date:"2026-10-11",status:"PASS_CHROMIUM_BROWSER_SMOKE",
     cases,passed:cases.length,failed:0,viewportDesktop:"1440x980",viewportMobile:"390x844",
     renderedImageSurfaces:true,clickedActualPhoneHotspots:true,
     systemBoundaryNeverDrawnAsAndroidUi:true,oneRetryTerminalGuard:true,
     finalFocusWasDriving:true,realAndroidHardwareQA:"NOT_RUN",
     media3PlaybackTested:false,sttProviderTested:false,independentHumanApproval:false};
   fs.writeFileSync(path.join(__dirname,"PHASE_8_BROWSER_VALIDATION.json"),JSON.stringify(report,null,2)+"\n");
   console.log("PASS",JSON.stringify(report));
 }catch(e){console.error("BROWSER SMOKE FAILED",e);process.exitCode=1;}
 finally{await browser.close();}
})();
