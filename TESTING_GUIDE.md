# Quick Testing Guide

## New Features to Test

### 1. Real-Time Bus Movement
**What to Test:**
- Bus marker appears on the map when navigation starts
- Marker moves smoothly (not jumping) between location updates
- Movement animation takes ~1.5 seconds
- No stuttering or lag in movement

**How to Test:**
- Start a trip in the app
- Open BusRoutesActivity to view map
- Start driving or simulate movement
- Observe the blue bus icon moving along the route

---

### 2. Bus Rotation / Direction
**What to Test:**
- Bus icon rotates to face direction of travel
- Rotation is smooth, not instant
- Arrow on bus icon points in the correct direction
- Works correctly during turns

**How to Test:**
- Drive straight → bus points forward
- Turn left → bus smoothly rotates left
- Turn right → bus smoothly rotates right
- U-turn → bus rotates 180°

---

### 3. 3D Map Experience
**What to Test:**
- Map is tilted at an angle (like Google Maps navigation)
- Can rotate map using two-finger twist
- Can tilt map using two-finger vertical swipe
- Bus marker maintains correct orientation when map rotates
- 3D buildings visible (if available in area)

**How to Test:**
- Pinch/rotate with two fingers → map rotates
- Two-finger swipe up/down → map tilts
- Observe bus marker stays correctly oriented
- Check camera angle is ~50° tilt

---

### 4. Traffic-Aware Routes
**What to Test:**
- Route shows different colors based on traffic:
  - **Blue** = normal/good traffic
  - **Yellow/Orange** = moderate traffic
  - **Red** = heavy traffic/congestion
- Colors update when route is recalculated
- Multiple segments with different colors visible

**How to Test:**
- View route before starting trip
- Look for colored segments (blue/yellow/red)
- Drive off-route and wait for recalculation
- Check if colors change with traffic conditions
- Compare with Google Maps for same route

---

### 5. Camera Following
**What to Test:**
- Camera automatically follows bus position
- Camera rotates to match bus direction
- Zoom level is close (18x) for street detail
- Camera movement is smooth, not jerky
- Camera keeps bus centered on screen

**How to Test:**
- Start navigation
- Observe camera centers on bus
- As bus moves, camera follows smoothly
- During turns, camera rotates with bus
- Bus stays in center of screen

---

### 6. Off-Route Handling
**What to Test:**
- App detects when driver goes off planned route
- Automatically recalculates route from current position
- New route shows updated traffic colors
- ETA updates with new route
- Smooth transition to new route

**How to Test:**
- Start navigation on planned route
- Deliberately drive on different road
- Wait 30-60 seconds
- App should show "recalculating" or update route
- New route should appear with traffic colors

---

### 7. ETA Display
**What to Test:**
- ETA shown at top of screen
- Updates based on traffic conditions
- Shows time in minutes
- Updates when route changes

**How to Test:**
- Check ETA when route loads
- Compare with expected time
- Drive and see if ETA decreases
- Go off-route and check if ETA updates

---

## Detailed Test Scenarios

### Scenario 1: Complete Trip Navigation
1. Log into driver app
2. Accept/view assigned trip
3. Tap to view route on map
4. Observe:
   - Full route appears with stops marked
   - Route has colored segments (blue/yellow/red)
   - Bus icon appears at start location
   - ETA displays at top
5. Start trip
6. Begin driving
7. Observe:
   - Bus moves smoothly along route
   - Bus rotates to face direction
   - Camera follows and rotates with bus
   - Map shows 3D tilted view
   - ETA counts down
8. Make a turn
9. Observe:
   - Bus rotates smoothly during turn
   - Camera follows the turn
   - No jerky movements
10. Continue to stop
11. Pick up passenger
12. Observe:
    - Route updates to show only "next stop"
    - Traffic colors still displayed
13. Complete trip

### Scenario 2: Off-Route Recovery
1. Start navigation as above
2. Deliberately drive on wrong road
3. Wait 30 seconds
4. Observe:
   - App detects off-route
   - Route recalculates automatically
   - New route appears from current position
   - Traffic colors update on new route
   - ETA updates
5. Follow new route
6. Observe:
   - Bus continues smooth movement
   - Everything works normally

### Scenario 3: Map Interaction
1. Start navigation
2. Use two fingers to rotate map
3. Observe:
   - Map rotates around bus
   - Bus icon maintains correct orientation
   - North arrow shows map rotation
4. Use two fingers to tilt map
5. Observe:
   - Map tilts to different angle
   - 3D effect visible
   - Bus remains visible
6. Zoom in/out
7. Observe:
   - Bus stays centered
   - Route remains visible

### Scenario 4: Traffic Conditions
1. Start navigation during peak hours
2. Observe route colors:
   - Main roads may show red (heavy traffic)
   - Some segments yellow (moderate)
   - Some segments blue (normal)
3. Compare with Google Maps
4. Check if colors match Google's traffic view
5. Wait 5-10 minutes
6. Go off-route to trigger recalculation
7. Check if traffic colors updated

---

## What Success Looks Like

✅ **Smooth Movement**: Bus glides along route, never jumps
✅ **Correct Rotation**: Bus always faces direction of travel
✅ **3D View**: Map shows tilted perspective like Google Maps
✅ **Colored Routes**: Blue/yellow/red segments clearly visible
✅ **Smooth Camera**: Camera follows bus without jerking
✅ **Auto-Reroute**: Detects off-route and recalculates
✅ **Updated ETA**: Time updates based on traffic
✅ **Map Rotation**: Can rotate map, bus stays oriented correctly

---

## Common Issues & Solutions

### Issue: Bus marker jumps instead of moving smoothly
**Solution**: Check location update frequency - should be 8 seconds for active trips

### Issue: Bus doesn't rotate
**Solution**: Check bearing calculation in location services - fallback logic should calculate from movement

### Issue: No traffic colors (all one color)
**Solution**: 
- Verify Google Maps API key is valid
- Check Directions API is enabled in Google Cloud Console
- Ensure `departure_time=now` is in API request

### Issue: Camera doesn't follow bus
**Solution**: Check camera animator is running - should animate with marker

### Issue: Map rotation doesn't work
**Solution**: Verify `isRotateGesturesEnabled = true` in map settings

### Issue: 3D icon not showing
**Solution**: Check if `ic_bus_marker_3d.xml` exists, will fallback to mipmap bus if not

---

## Performance Checks

Monitor these during testing:

1. **Battery Usage**: Should be reasonable for GPS app
2. **Network Usage**: API calls made only on route changes
3. **Memory**: No memory leaks from animators
4. **CPU**: Animations smooth without high CPU
5. **GPS Lock**: Maintains good GPS signal
6. **Frame Rate**: Map rendering stays at 60fps

---

## Comparison with Google Maps

Test side-by-side with Google Maps navigation:

| Feature | Google Maps | Shuttle Driver | Match? |
|---------|-------------|----------------|--------|
| Smooth movement | ✓ | ✓ | ✅ |
| Marker rotation | ✓ | ✓ | ✅ |
| 3D perspective | ✓ | ✓ | ✅ |
| Traffic colors | ✓ | ✓ | ✅ |
| Camera follow | ✓ | ✓ | ✅ |
| Auto-reroute | ✓ | ✓ | ✅ |
| Map rotation | ✓ | ✓ | ✅ |

---

## Test Devices

Recommended to test on:
- At least one physical Android device (GPS required)
- Android 6.0+ (API 23+)
- Device with good GPS signal
- Area with varied traffic conditions
- Different screen sizes

---

## Debugging Tips

Enable developer logging to see:
- Location updates frequency
- Bearing calculations
- Route API responses
- Animation timings

Check LogCat for:
- `BusRoutesActivity`: Route drawing, marker updates
- `BGLocationUpdateService`: Location updates during trip
- `LocationUpdateService`: Location updates when online

---

## Report Template

After testing, report results:

**Date**: [Date]
**Device**: [Make/Model]
**Android Version**: [Version]
**GPS Quality**: [Good/Fair/Poor]
**Location**: [City/Area]

**Test Results**:
- [ ] Real-time movement - PASS/FAIL
- [ ] Bus rotation - PASS/FAIL
- [ ] 3D view - PASS/FAIL
- [ ] Traffic colors - PASS/FAIL
- [ ] Camera follow - PASS/FAIL
- [ ] Off-route handling - PASS/FAIL
- [ ] ETA accuracy - PASS/FAIL

**Issues Found**:
1. [Description]
2. [Description]

**Notes**:
[Any additional observations]

---

## Quick Reference - Color Meanings

🔵 **Blue Route** = Normal traffic, good flow, recommended speed
🟡 **Yellow/Orange Route** = Moderate traffic, slower than normal
🔴 **Red Route** = Heavy traffic, significant delays, congestion

Match with Google Maps for verification!
