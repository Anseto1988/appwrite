
// Minimal test without any dependencies
module.exports = async function(req, res) {
    console.log('Minimal test function started');
    res.json({
        success: true,
        message: 'Minimal test working',
        timestamp: new Date().toISOString()
    });
};